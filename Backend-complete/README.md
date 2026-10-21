# ONN Call Backend

REST API for the ONN Call marketplace: clients request home services (plumbing, electrical,
carpentry, daily wage work), verified providers accept them, and the job is confirmed with a
completion code, then reviewed.

Stack: Java 21, Spring Boot 3.4, Spring Security (JWT), Spring Data JPA, PostgreSQL (production)
with PostgreSQL in every environment (H2 is used only by automated tests).

---

## 1. Run it

The database is PostgreSQL. Its files are stored in `./data/postgres` inside the project folder.

### Start the database

You need Docker. Run this from the project folder:

```bash
docker compose up -d db
```

Check it is ready with `docker compose ps` (the `db` service should show `healthy`).
Without Docker, install PostgreSQL 16, create a database named `onncall` with user `postgres`
and password `postgres` on port 5432. The application uses those values by default.

### Start the API

```bash
ADMIN_PHONE=+919000000001 mvn spring-boot:run
```

The `dev` profile is the default. It returns the login code in API responses so you can test
without an SMS provider. The API runs at `http://localhost:8080`. Tables are created automatically
on first run.

### Production-style (API in Docker too)

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
docker compose up --build
```

---

## 2. Configuration

| Variable | Default | Purpose |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` (OTP returned in responses) or `prod` (OTP hidden) |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | local Postgres | Database connection (prod) |
| `DDL_AUTO` | `update` | Hibernate schema mode |
| `JWT_SECRET` | dev value | **Must be set in production.** At least 32 characters |
| `JWT_EXPIRATION_MINUTES` | `10080` (7 days) | Token lifetime |
| `CORS_ALLOWED_ORIGINS` | localhost 3000, 5173, 8081 | Comma-separated frontend origins |
| `UPLOAD_DIR` | `./uploads` | Where photos and documents are stored |
| `ADMIN_PHONE` | empty | Creates an ADMIN account at startup if set |
| `AUTH_PROVIDER` | `firebase` (dev profile: `dev`) | `firebase` verifies real Firebase sign-ins |
| `OTP_ENABLED` | `false` (dev profile: `true`) | Enables the older code-login endpoints |
| `GOOGLE_APPLICATION_CREDENTIALS` | none | Path to the Firebase service-account JSON file (production) |
| `PORT` | `8080` | HTTP port |

The `prod` profile is not yet a separate file. Set `SPRING_PROFILES_ACTIVE=prod` and make sure
`app.otp.expose-in-response` is `false` (it is by default outside `dev`).

---

## 3. Authentication

Users sign in with their phone number. The SMS code is sent and checked by **Firebase Authentication**.
The backend never sends SMS itself. It verifies the Firebase sign-in token and then issues its own JWT.

### Production flow (Firebase)

1. The frontend signs the user in with Firebase phone auth. Firebase texts the code and checks it.
2. The frontend gets the Firebase ID token and sends it to the backend:

```
POST /api/auth/firebase/login     {"idToken": "<firebase id token>"}
  200 -> {"accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 604800,
          "user": {"id": 1, "phone": "+919876543210", "role": "CLIENT", "profileId": 1}}
  404 -> no account yet: go to step 3

3. POST /api/auth/firebase/register
     {"idToken": "<firebase id token>", "role": "CLIENT", "name": "Asha",
      "address": "...", "latitude": 33.7, "longitude": 75.1}
     (PROVIDER needs "name" and may include "bio"; DISTRIBUTOR needs "businessName")
  201 -> same response as login
```

4. Every other call sends `Authorization: Bearer <accessToken>`.
5. `GET /api/auth/me` returns who you are and your `profileId`.

Frontend example (Firebase Web SDK, modular API):

```js
import { getAuth, RecaptchaVerifier, signInWithPhoneNumber } from "firebase/auth";

const auth = getAuth(app);
const verifier = new RecaptchaVerifier(auth, "recaptcha-container", { size: "invisible" });
const confirmation = await signInWithPhoneNumber(auth, "+919876543210", verifier);
const credential = await confirmation.confirm(codeTypedByUser);   // the 6-digit SMS code
const idToken = await credential.user.getIdToken();
// then POST idToken to /api/auth/firebase/login (or /register on 404)
```

### Development flow (no Firebase needed)

The `dev` profile accepts a fake token: `dev-token:+919876543210`. Use it in the same
endpoints for local testing. It refuses to start under the `prod` profile.

```
POST /api/auth/firebase/register  {"idToken": "dev-token:+919876543210", "role": "CLIENT", "name": "Asha"}
```

The older code login (`/api/auth/otp/request`, `/api/auth/login`, `/api/auth/register`) still works in
dev and returns the code as `devOtp`. It is turned off in production (`OTP_ENABLED=false`).

### Firebase setup (one time, by the project owner)

1. Go to https://console.firebase.google.com, create a project, and open **Authentication → Sign-in method**.
   Enable **Phone**.
2. Check the current limits on the free Spark plan. Sending SMS to Indian numbers may need the paid Blaze plan.
   Confirm this before launch.
3. **Project settings → Service accounts → Generate new private key.** Save the JSON file as
   `secrets/firebase-service-account.json` inside the project. This file is a secret. It is in `.gitignore`.
4. Set `GOOGLE_APPLICATION_CREDENTIALS` to that file's path. For local runs:
   `export GOOGLE_APPLICATION_CREDENTIALS="$PWD/secrets/firebase-service-account.json"`
5. **Project settings → General** shows the web config (apiKey, authDomain, projectId, appId). Give those to the
   frontend team. They are not secret.
6. In **Authentication → Settings → Authorized domains**, add `localhost` and the frontend's deployed domain.

Backend environment variables for production: `AUTH_PROVIDER=firebase`, `OTP_ENABLED=false`, and
`GOOGLE_APPLICATION_CREDENTIALS` pointing at the service-account file.

---

## 4. Roles and what they can do

- **CLIENT**: request services, start jobs, generate completion codes, review completed jobs, manage own profile.
- **PROVIDER**: list the services they offer, accept or reject requests, move jobs along, verify completion codes, upload documents and availability.
- **DISTRIBUTOR**: a business profile; can manage its own details.
- **ADMIN**: reviews provider documents and sets provider verification, manages services and prices, and can see everything.

Access is checked in two places: `SecurityConfig` (which roles can call which URL), and the services,
through `AccessGuard` (is this your client/provider record, or are you on this job?).

---

## 5. Workflows

**Service request:** `PENDING` → `ACCEPTED` or `REJECTED` (provider), or `CANCELLED` (client or provider, while PENDING or ACCEPTED).

**Job:** `CREATED` → `ACCEPTED` → `ARRIVED` → `IN_PROGRESS` → `COMPLETED`. Any open job can go to `CANCELLED`.

1. Provider accepts the request (`PUT /api/service-requests/{id}/status?status=ACCEPTED`).
2. Either party starts the job (`POST /api/jobs?serviceRequestId={id}`). It starts as `CREATED`.
3. Provider moves it on: `ACCEPTED` → `ARRIVED` → `IN_PROGRESS` (`PUT /api/jobs/{id}/status?status=...`).
   `IN_PROGRESS` sets `startedAt`.
4. Client generates a code once the provider has arrived (`POST /api/completion-verification/generate/{jobId}`).
   The response contains `otpCode`. The client shows it to the provider.
5. Provider enters it (`POST /api/completion-verification/verify/{jobId}?otp=...`).
6. Provider marks the job `COMPLETED`. This is refused until step 5 has succeeded. It sets `completedAt`
   and adds 1 to the provider's `completedJobs`.
7. Client reviews the job (`POST /api/reviews`). The provider's `rating` is updated to the average.

Every status change is recorded in `job_status_history`.

Invalid moves return `409 Conflict`. Other rule failures return `400`, `403`, or `404`.

---

## 6. Errors

Every error uses the same JSON shape:

```json
{
  "error": "Not Found",
  "message": "Job not found with id: 12",
  "path": "/api/jobs/12",
  "timestamp": "2026-10-21T10:15:30.123Z"
}
```

Validation errors (400) also include `"fields": {"phone": "must not be blank"}`.

| Status | Meaning |
|---|---|
| 400 | Bad input, or a rule was not met (for example, wrong OTP, job not ready) |
| 401 | Missing, invalid, or expired token |
| 403 | Logged in, but not allowed to do this |
| 404 | Record not found |
| 409 | Conflict (duplicate, or the status change is not allowed right now) |
| 413 | Upload too large |
| 429 | Too many attempts or requests (OTP cooldown, wrong-code limit) |
| 500 | Server error (details are logged, not returned) |

---

## 7. Endpoints

Each request needs `Authorization: Bearer <token>` unless it says *Public*.
Query parameters are shown after `?`. Multipart endpoints are noted.
Responses for entities (client, provider, job, request, etc.) are the JSON of the stored records.
A `Job` includes nested `client`, `provider`, `service` and `serviceRequest` objects.
Photos and documents include a `url` field, which points to `/api/files/...`.

| Method | Path | Who can call it | What it does |
|---|---|---|---|
| POST | `/api/auth/otp/request` | Public | Send a 6-digit code to a phone number |
| POST | `/api/auth/login` | Public | Log in with phone + code (404 if no account) |
| POST | `/api/auth/register` | Public | Create CLIENT / PROVIDER / DISTRIBUTOR account with phone + code (201) |
| GET | `/api/auth/me` | Logged in | Current user and profileId |
| GET | `/api/services` | Public | List service categories |
| GET | `/api/services/active` | Public | Active service categories |
| GET | `/api/services/{id}` | Public | One service |
| GET | `/api/services/type/{type}` | Public | Service by type (PLUMBER, ELECTRICIAN, CARPENTER, DAILY_WAGER) |
| POST, PUT, DELETE | `/api/services[/{id}]` | Admin | Manage service categories and prices |
| GET | `/api/providers` | Logged in | List providers |
| GET | `/api/providers/{id}` | Logged in | One provider |
| PUT | `/api/providers/{id}` | Owner provider or admin | Update own profile and status (verification is NOT editable here) |
| POST | `/api/providers` | Admin | Create provider directly |
| DELETE | `/api/providers/{id}` | Admin | Delete provider |
| GET | `/api/clients` | Admin | List clients |
| GET | `/api/clients/{id}` | Owner client or admin | One client |
| PUT | `/api/clients/{id}` | Owner client or admin | Update own profile |
| POST | `/api/clients` | Admin | Create client directly |
| DELETE | `/api/clients/{id}` | Admin | Delete client |
| GET | `/api/distributors/active` | Logged in | Active distributors |
| GET | `/api/distributors` | Admin | List distributors |
| GET | `/api/distributors/{id}` | Owner distributor or admin | One distributor |
| GET | `/api/distributors/user/{userId}` | Self or admin | Distributor by user id |
| PUT | `/api/distributors/{id}` | Owner distributor (status: admin only) | Update distributor |
| POST | `/api/distributors` | Admin | Create distributor directly |
| DELETE | `/api/distributors/{id}` | Admin | Delete distributor |
| GET | `/api/provider-services` | Admin | All provider-service links |
| POST | `/api/provider-services?providerId=&serviceId=` | Owner provider | Add a service the provider offers |
| GET | `/api/provider-services/{id}` | Logged in | One link |
| GET | `/api/provider-services/provider/{providerId}` | Logged in | Services a provider offers |
| GET | `/api/provider-services/provider/{providerId}/active` | Logged in | Active services for a provider |
| GET | `/api/provider-services/service/{serviceId}` | Logged in | Providers offering a service |
| PUT | `/api/provider-services/{id}/status?status=` | Owner provider | ACTIVE or INACTIVE |
| DELETE | `/api/provider-services/{id}` | Owner provider | Remove a service offering |
| GET | `/api/provider-availability/provider/{providerId}` | Logged in | Weekly availability |
| POST | `/api/provider-availability` | Owner provider | Add an availability slot |
| PUT | `/api/provider-availability/{id}/provider/{providerId}` | Owner provider | Edit a slot |
| DELETE | `/api/provider-availability/{id}/provider/{providerId}` | Owner provider | Delete a slot |
| POST | `/api/provider-documents/upload (multipart: providerId, documentType, file)` | Owner provider | Upload a verification document (PDF/JPG/PNG/WEBP, max 5 MB) |
| GET | `/api/provider-documents/provider/{providerId}` | Owner provider | A provider's documents |
| GET | `/api/admin/provider-documents/pending` | Admin | Documents awaiting review |
| PATCH | `/api/admin/provider-documents/{documentId}/review?approved=&rejectionReason=` | Admin | Approve or reject a document |
| PATCH | `/api/admin/providers/{providerId}/verification?status=` | Admin | Set a provider to VERIFIED, REJECTED or PENDING |
| GET | `/api/service-requests/{id}` | Client or provider on it | One request |
| POST | `/api/service-requests?clientId=&providerId=&serviceId=&address=&latitude=&longitude=&description=` | Owner client | Request a service from a verified provider |
| GET | `/api/service-requests/client/{clientId}` | Owner client | A client's requests |
| GET | `/api/service-requests/provider/{providerId}` | Owner provider | A provider's requests |
| GET | `/api/service-requests/provider/{providerId}/pending` | Owner provider | A provider's pending requests |
| PUT | `/api/service-requests/{id}/status?status=` | Provider: ACCEPTED/REJECTED; client or provider: CANCELLED | Change request status |
| GET | `/api/service-requests` | Admin | All requests |
| GET | `/api/service-requests/status/{status}` | Admin | Requests by status |
| DELETE | `/api/service-requests/{id}` | Admin | Delete a request |
| POST | `/api/jobs?serviceRequestId=` | Client or provider on the request | Start a job from an ACCEPTED request |
| GET | `/api/jobs/{id}` | Client or provider on the job | One job |
| GET | `/api/jobs/request/{serviceRequestId}` | Client or provider on the job | Job for a request |
| GET | `/api/jobs/client/{clientId}` | Owner client | A client's jobs |
| GET | `/api/jobs/provider/{providerId}` | Owner provider | A provider's jobs |
| GET | `/api/jobs` | Admin | All jobs |
| GET | `/api/jobs/status/{status}` | Admin | Jobs by status |
| PUT | `/api/jobs/{id}/status?status=` | Provider (ACCEPTED, ARRIVED, IN_PROGRESS, COMPLETED); either party (CANCELLED) | Move the job along its lifecycle |
| PUT | `/api/jobs/{id}/price?finalPrice=` | Provider | Set the final price before the job ends |
| DELETE | `/api/jobs/{id}` | Admin | Delete a job |
| POST | `/api/completion-verification/generate/{jobId}` | Client on the job | Create the 6-digit completion code (job must be ARRIVED or IN_PROGRESS) |
| POST | `/api/completion-verification/verify/{jobId}?otp=` | Provider on the job | Confirm the code (needed before COMPLETED) |
| GET | `/api/job-status-history/job/{jobId}` | Client or provider on the job | Status history of a job |
| GET | `/api/job-status-history/{id}` | Client or provider on the job | One history entry |
| POST | `/api/job-status-history` | Admin | Add a manual history entry |
| POST | `/api/job-photos/upload (multipart: jobId, description, file)` | Client or provider on the job | Upload a job photo (max 5 MB) |
| GET | `/api/job-photos/job/{jobId}` | Client or provider on the job | Photos of a job |
| DELETE | `/api/job-photos/{photoId}` | Client or provider on the job | Delete a photo |
| GET | `/api/files/{fileName}` | Logged in | Download an uploaded file (use the "url" field of photos and documents) |
| POST | `/api/reviews` | Client on a COMPLETED job | Rate a job (1-5) once |
| GET | `/api/reviews/provider/{providerId}` | Logged in | Reviews for a provider |
| GET | `/api/reviews/job/{jobId}` | Client or provider on the job | Reviews for a job |
| GET | `/api/notifications/user/{userId}` | Self or admin | All notifications |
| GET | `/api/notifications/user/{userId}/unread` | Self or admin | Unread notifications |
| PATCH | `/api/notifications/{notificationId}/read?userId=` | Self or admin | Mark as read |
| DELETE | `/api/notifications/{notificationId}?userId=` | Self or admin | Delete a notification |
| POST | `/api/notifications` | Admin | Send a notification |
| GET | `/api/users` | Admin | List accounts |
| GET | `/api/users/{id}` | Admin | One account |
| POST, PUT, DELETE | `/api/users[/{id}]` | Admin | Manage accounts |

| POST | `/api/auth/firebase/login` | Public | Log in with a Firebase sign-in token (404 if no account yet) |
| POST | `/api/auth/firebase/register` | Public | Create an account with a Firebase sign-in token (201) |

---

## 8. Project layout

```
src/main/java/official/onncallbackend/
  Auth/            OTP, registration, login, JWT, AccessGuard (ownership checks)
  Config/          SecurityConfig, FileStorageService, FileController, DataSeeder
  common/          ApiException, ErrorResponse, GlobalExceptionHandler
  Client/ Provider/ Distributor/ User/
  ServiceRequest/  Job/ JobStatusHistory/ CompletionVerification/
  ProviderService/ ProviderAvailability/ ProviderDocument/ JobPhoto/
  Review/ Notification/ Admin/ Service/
src/test/java/...  Integration test of the auth flow and security rules; unit tests for job rules
```

---

## 9. Testing

```bash
mvn test                                   # automated tests
ADMIN_PHONE=+919000000001 ./scripts/smoke-test.sh   # live end-to-end check (server must be running)
```

The integration tests use an in-memory H2 database, so they do not need PostgreSQL running. They call the real endpoints
through MockMvc. `JobStatusRulesTest` checks the job state machine directly. The smoke test in
`scripts/smoke-test.sh` runs the full marketplace flow against a live server; use it as the
acceptance check before each frontend integration milestone.

---

## 10. Before going live

These are known gaps. Each one should be closed before real users sign up.

1. **Firebase set-up.** Complete the Firebase setup in section 3 (project, Phone sign-in enabled,
   service-account key). Until then, production sign-in cannot work. Confirm Firebase's SMS limits and
   pricing for India before launch.
2. **Database migrations.** The schema is created by Hibernate (`ddl-auto=update`). Add Flyway, and set
   `DDL_AUTO=validate` in production.
3. **Rate limiting.** Add limits on `/api/auth/otp/request` per IP as well as per phone.
4. **File storage.** Files are stored on local disk. For more than one server, move this behind the
   `FileStorageService` interface to an object store (S3 or similar), and serve files through signed URLs.
5. **Ownership on file download.** `/api/files/{fileName}` only requires a login. The file names are random
   UUIDs, but anyone who has a link can open it.
6. **Verification process.** An admin approves a provider with `PATCH /api/admin/providers/{providerId}/verification?status=VERIFIED`
   after reviewing their documents. The document review step is manual, and no notification is sent to the provider yet.
7. **Role changes.** A user's role is fixed in the JWT until they log in again.
8. **Placeholder prices.** `DataSeeder` seeds four services with placeholder prices. Update them with
   `PUT /api/services/{id}`.
9. **Secrets.** Replace the dev `JWT_SECRET` before deployment.
10. **Run the verification.** Run `mvn test` and the smoke test (section 1) on the target machine before
    integration. They are the first time this code runs with the full Spring dependencies.
