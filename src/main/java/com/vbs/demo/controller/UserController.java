package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import com.vbs.demo.dto.LoginDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")

public class UserController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HistoryRepo historyRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        userRepo.save(user);

        History h1 = new History();
        h1.setDescripton("User Self Created: "+user.getUsername());
        historyRepo.save(h1);

        return "Signup Successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u) {

        User user = userRepo.findByUsername(u.getUsername());

        if (user == null) {
            return "User Not Found";   // ✅ semicolon fixed
        }

        if (!u.getPassword().equals(user.getPassword())) {
            return "Password Incorrect";
        }

        if (!u.getRole().equals(user.getRole())) {
            return "Role Incorrect";
        }

        return String.valueOf(user.getId()); // ✅ success
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User Not Found"));

        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj){
        User user = userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not Found"));

        History h1 = new History();

        if(obj.getKey().equalsIgnoreCase("name")){
            if(user.getName().equalsIgnoreCase(obj.getValue())) return "Cannot be same";

            h1.setDescripton("User updated Name from: "+user.getName()+" to "+obj.getValue());

            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password")){
            h1.setDescripton("User "+user.getUsername()+" updated Password!");

            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email")){
            User user2 = userRepo.findByEmail(obj.getValue());
            if(user2 != null) return "Email already exists";

            h1.setDescripton("User updated Email from: "+user.getEmail()+" to "+obj.getValue());

            user.setEmail(obj.getValue());
        }
        else{
            return "Invalid Input";
        }
        historyRepo.save(h1);
        userRepo.save(user);
        return "Profile Update Done Successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user, @PathVariable int adminId){

        History h1 = new History();
        h1.setDescripton("Admin: "+adminId+" Created User: "+user.getUsername());
        historyRepo.save(h1);

        userRepo.save(user);
        return "Successfully Added!";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy, @RequestParam String order){
        Sort sort;
        if(order.equalsIgnoreCase("desc")){
            sort = Sort.by(sortBy).descending();
        }
        else{
            sort = Sort.by(sortBy).ascending();
        }
        return  userRepo.findAllByRole("customer", sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUsersByKeyword(@PathVariable String keyword){
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }

    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUser(@PathVariable int userId, @PathVariable int adminId){
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException("Not found"));

        if(user.getBalance()>0){
            return "Balance should be 0";
        }

        History h1 = new History();
        h1.setDescripton("Admin: "+adminId+" Deleted User: "+userId);
        historyRepo.save(h1);

        userRepo.delete(user);
        return "User Deleted Successfully";
    }
}