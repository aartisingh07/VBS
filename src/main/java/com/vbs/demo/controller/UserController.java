package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.Admin;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.AdminRepo;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import com.vbs.demo.dto.LoginDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")

public class UserController {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private HistoryRepo historyRepo;

    @Autowired
    private AdminRepo adminRepo;;

    @Autowired
    private TransactionRepo transactionRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        if (adminRepo.existsByUsername(user.getUsername())) {
            return "REGISTERED SUCCESSFULLY";
        }

        if (userRepo.existsByUsername(user.getUsername())) {
            return "ALREADY REGISTERED";
        }
        Admin admin = new Admin();
        if(user.getRole().equals("admin")){

            admin.setUsername(user.getUsername());
            admin.setName(user.getName());
            admin.setEmail(user.getEmail());
            admin.setPassword(user.getPassword());
            admin.setRole("admin");

            adminRepo.save(admin);
        }
        else{
            userRepo.save(user);
        }

        History h1 = new History();
        h1.setDescription("User Self Created: "+user.getUsername());
        historyRepo.save(h1);

        return "Signup Successful";
    }


    @PostMapping("/login")
    public String login(@RequestBody LoginDto u) {
        User user = userRepo.findByUsername(u.getUsername());
        Admin admin =  adminRepo.findByUsername(u.getUsername());

        if(u.getRole().equals("admin")){
            if(admin == null){
                return "Admin Not Found";
            }
            if(!u.getPassword().equals(admin.getPassword())){
                return "Password Incorrect";
            }
            return String.valueOf(admin.getId());
        }
        else{
            if (user == null) {
                return "User Not Found";
            }
            if (!u.getPassword().equals(user.getPassword())) {
                return "Password Incorrect";
            }
            if (!u.getRole().equals(user.getRole())) {
                return "Role Incorrect";
            }
            return String.valueOf(user.getId());
        }
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

            h1.setDescription("User updated Name from: "+user.getName()+" to "+obj.getValue());

            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password")){
            h1.setDescription("User "+user.getUsername()+" updated Password!");

            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email")){
            User user2 = userRepo.findByEmail(obj.getValue());
            if(user2 != null) return "Email already exists";

            h1.setDescription("User updated Email from: "+user.getEmail()+" to "+obj.getValue());

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

        if (userRepo.existsByUsername(user.getUsername())) {
            return "Username already exists";
        }
        user.setRole("customer");

        History h1 = new History();
        h1.setDescription("Admin: "+adminId+" Created User: "+user.getUsername());
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
        h1.setDescription("Admin: "+adminId+" Deleted User: "+userId);
        historyRepo.save(h1);

        userRepo.delete(user);
        return "User Deleted Successfully";
    }

    @GetMapping("/notifications/{userId}")
    public String getNotifications(@PathVariable int userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        long MIN_BALANCE = 500;
        if (user.getBalance() < MIN_BALANCE) {
            return "⚠️ Your balance is below minimum limit. Please maintain balance to avoid account freeze.";
        }

        if (user.getCreatedAt().isBefore(LocalDateTime.now().minusDays(30))) {
            return "⚠️ Your account has been inactive for a long time. Please perform a transaction.";
        }

        return "No notifications";
    }

    @GetMapping("/notifications/unread-count/{userId}")
    public int getNotificationCount(@PathVariable int userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        int count = 0;
        if (user.getBalance() < 500) count++;
        if (user.getCreatedAt().isBefore(LocalDateTime.now().minusDays(30))) count++;
        return count;
    }
}