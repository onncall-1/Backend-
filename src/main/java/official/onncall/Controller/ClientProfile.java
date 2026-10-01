package official.onncall.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/cl")
public class ClientProfile{
    @PostMapping
    public ClientProfile(){
        return ;
    }
    @PostMapping
    public int id(){
        return 1 ;
    }
    @PostMapping
    public String name(){
        return null ;
    }
     @PostMapping
    public String ProfilePhoto(){
        return null ;
    }

    @PostMapping
    public String address(){
        return null ;
    }

    @PostMapping
    public String latutude(){
        return null ;

    }
    @PostMapping
    public String longitute(){
        return null ;
    }

    @PostMapping
    public String createdAt(){
        return null ;
    }



}
