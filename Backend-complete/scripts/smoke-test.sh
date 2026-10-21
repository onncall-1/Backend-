#!/usr/bin/env bash
# End-to-end smoke test for the ONN Call API.
#
# Runs the full marketplace workflow against a LIVE server and checks each step,
# plus the rules that must reject bad requests. Exit code 0 = everything passed.
#
# Usage:
#   ADMIN_PHONE=+919000000001 ./scripts/smoke-test.sh [http://localhost:8080]
#
# The server must be started with the same ADMIN_PHONE value, in the dev profile
# (so the OTP is returned in the response):
#   ADMIN_PHONE=+919000000001 mvn spring-boot:run

set -u
BASE="${1:-http://localhost:8080}"
ADMIN_PHONE="${ADMIN_PHONE:?Set ADMIN_PHONE to the same value the server was started with}"

PASS=0
FAIL=0
STATUS=""
BODY=""

req() {  # req METHOD PATH [TOKEN] [JSON_BODY]
  local method="$1" path="$2" token="${3:-}" data="${4:-}"
  local args=(-s -o /tmp/smoke_body.$$ -w "%{http_code}" -X "$method" "$BASE$path" -H "Content-Type: application/json")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer $token")
  [ -n "$data" ] && args+=(-d "$data")
  STATUS=$(curl "${args[@]}")
  BODY=$(cat /tmp/smoke_body.$$ 2>/dev/null)
}

jget() {  # jget dotted.path   (reads BODY)
  printf '%s' "$BODY" | python3 -c '
import sys, json
d = json.load(sys.stdin)
for k in sys.argv[1].split("."):
    d = d[int(k)] if isinstance(d, list) else d[k]
print(d)' "$1" 2>/dev/null
}

check() {  # check "description" EXPECTED_STATUS
  if [ "$STATUS" = "$2" ]; then
    PASS=$((PASS + 1)); echo "  PASS  $1 ($STATUS)"
  else
    FAIL=$((FAIL + 1)); echo "  FAIL  $1 expected $2, got $STATUS -> $BODY"
  fi
}

random_phone() { echo "+9199$(printf '%08d' $((RANDOM * RANDOM % 100000000)))"; }

# otp_for PHONE -> sets OTP
otp_for() {
  req POST /api/auth/otp/request "" "{\"phone\":\"$1\"}"
  check "OTP requested for $1" 200
  OTP=$(jget devOtp)
  if [ -z "$OTP" ] || [ "$OTP" = "None" ]; then
    echo "  ABORT server did not return devOtp. Is it running the dev profile?"; exit 2
  fi
}

# register_as PHONE ROLE NAME -> sets TOKEN, PROFILE_ID
register_as() {
  otp_for "$1"
  req POST /api/auth/register "" "{\"phone\":\"$1\",\"otp\":\"$OTP\",\"role\":\"$2\",\"name\":\"$3\",\"businessName\":\"$3\"}"
  check "register $2 ($3)" 201
  TOKEN=$(jget accessToken)
  PROFILE_ID=$(jget user.profileId)
}

echo "== ONN Call smoke test against $BASE =="

echo "[1] Server health"
req GET /actuator/health
check "health endpoint" 200

echo "[2] Service catalogue (public)"
req GET /api/services
check "list services" 200
req GET /api/services/type/PLUMBER
check "get PLUMBER service" 200
SERVICE_ID=$(jget id)

echo "[3] Admin login"
otp_for "$ADMIN_PHONE"
req POST /api/auth/login "" "{\"phone\":\"$ADMIN_PHONE\",\"otp\":\"$OTP\"}"
check "admin login" 200
ADMIN_TOKEN=$(jget accessToken)

echo "[4] Accounts"
CLIENT_PHONE=$(random_phone); PROVIDER_PHONE=$(random_phone)
register_as "$CLIENT_PHONE" CLIENT "Smoke Client";  CLIENT_TOKEN=$TOKEN;  CLIENT_ID=$PROFILE_ID
register_as "$PROVIDER_PHONE" PROVIDER "Smoke Provider"; PROVIDER_TOKEN=$TOKEN; PROVIDER_ID=$PROFILE_ID

req GET /api/auth/me "$CLIENT_TOKEN"
check "GET /me as client" 200

echo "[5] Business rule: unverified provider cannot receive requests"
req POST "/api/service-requests?clientId=$CLIENT_ID&providerId=$PROVIDER_ID&serviceId=$SERVICE_ID&address=Lane-1" "$CLIENT_TOKEN"
check "request to unverified provider is refused" 403

echo "[6] Admin verifies provider; provider offers PLUMBER"
req PATCH "/api/admin/providers/$PROVIDER_ID/verification?status=VERIFIED" "$ADMIN_TOKEN"
check "admin verifies provider" 200
req POST "/api/provider-services?providerId=$PROVIDER_ID&serviceId=$SERVICE_ID" "$PROVIDER_TOKEN"
check "provider adds PLUMBER" 200

echo "[7] Request, accept, start job"
req POST "/api/service-requests?clientId=$CLIENT_ID&providerId=$PROVIDER_ID&serviceId=$SERVICE_ID&address=Lane-1&description=Leaking%20tap" "$CLIENT_TOKEN"
check "client creates request" 200
REQUEST_ID=$(jget id)

req PUT "/api/service-requests/$REQUEST_ID/status?status=ACCEPTED" "$CLIENT_TOKEN"
check "client cannot accept its own request" 403
req PUT "/api/service-requests/$REQUEST_ID/status?status=ACCEPTED" "$PROVIDER_TOKEN"
check "provider accepts request" 200

req POST "/api/jobs?serviceRequestId=$REQUEST_ID" "$CLIENT_TOKEN"
check "client starts job from accepted request" 200
JOB_ID=$(jget id)

echo "[8] Job lifecycle with completion code"
req PUT "/api/jobs/$JOB_ID/status?status=COMPLETED" "$PROVIDER_TOKEN"
check "cannot skip to COMPLETED" 409

req PUT "/api/jobs/$JOB_ID/status?status=ACCEPTED" "$PROVIDER_TOKEN"
check "job CREATED -> ACCEPTED" 200
req PUT "/api/jobs/$JOB_ID/status?status=ARRIVED" "$PROVIDER_TOKEN"
check "job ACCEPTED -> ARRIVED" 200

req POST "/api/completion-verification/generate/$JOB_ID" "$CLIENT_TOKEN"
check "client generates completion code" 200
CODE=$(jget otpCode)
WRONG=000000
[ "$CODE" = "$WRONG" ] && WRONG=111111

req POST "/api/completion-verification/verify/$JOB_ID?otp=$WRONG" "$PROVIDER_TOKEN"
check "wrong code is refused" 400
req POST "/api/completion-verification/verify/$JOB_ID?otp=$CODE" "$PROVIDER_TOKEN"
check "provider enters correct code" 200

req PUT "/api/jobs/$JOB_ID/status?status=IN_PROGRESS" "$PROVIDER_TOKEN"
check "job ARRIVED -> IN_PROGRESS" 200
req PUT "/api/jobs/$JOB_ID/status?status=COMPLETED" "$PROVIDER_TOKEN"
check "job IN_PROGRESS -> COMPLETED (after verification)" 200

echo "[9] Review and rating"
req POST /api/reviews "$CLIENT_TOKEN" "{\"jobId\":$JOB_ID,\"rating\":5,\"comment\":\"Great work\"}"
check "client reviews completed job" 200
req POST /api/reviews "$CLIENT_TOKEN" "{\"jobId\":$JOB_ID,\"rating\":4}"
check "second review of same job is refused" 409
req GET "/api/providers/$PROVIDER_ID" "$CLIENT_TOKEN"
check "provider rating updated" 200
RATING=$(jget rating)
[ "$RATING" = "5.0" ] && echo "  PASS  provider rating is 5.0" && PASS=$((PASS + 1)) \
  || { echo "  FAIL  provider rating expected 5.0, got $RATING"; FAIL=$((FAIL + 1)); }

echo "[10] Access control"
req GET /api/users
check "admin endpoint without token" 401
req GET /api/users "$CLIENT_TOKEN"
check "admin endpoint as client" 403
req GET "/api/clients/$CLIENT_ID" "$PROVIDER_TOKEN"
check "provider cannot read a client profile" 403
req GET "/api/jobs/$JOB_ID" "$CLIENT_TOKEN"
check "client can read own job" 200

echo
echo "== Result: $PASS passed, $FAIL failed =="
rm -f /tmp/smoke_body.$$
[ "$FAIL" -eq 0 ]
