package com.basal.controller;

import com.basal.entity.User;
import com.basal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ========================================================
    // 1. /users -> Tüm kullanıcıları listele
    // ========================================================
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Ekstra: /users/{id} -> Tekil kullanıcı getir
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Kullanıcı bulunamadı. ID: " + id)));
    }

    // ========================================================
    // 2. /users/add -> Yeni kullanıcı ekle
    // ========================================================
    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // ========================================================
    // 3. /users/remove -> Kullanıcı sil
    //    Desteklenenler:
    //    - DELETE /users/remove/{id} (veya POST /users/remove/{id})
    //    - DELETE /users/remove?id={id} (veya POST /users/remove?id={id})
    // ========================================================
    @RequestMapping(value = {"/remove/{id}", "/remove"}, method = {RequestMethod.DELETE, RequestMethod.POST})
    public ResponseEntity<?> removeUser(
            @PathVariable(required = false) Long id,
            @RequestParam(required = false) Long idParam) {

        Long targetId = (id != null) ? id : idParam;
        if (targetId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Silinecek kullanıcı ID'si belirtilmelidir."));
        }

        boolean deleted = userService.deleteUser(targetId);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Kullanıcı başarıyla silindi. ID: " + targetId));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Silinecek kullanıcı bulunamadı. ID: " + targetId));
        }
    }

    // ========================================================
    // 4. /users/update -> Kullanıcı güncelle
    //    Desteklenenler:
    //    - PUT  /users/update/{id} (veya POST /users/update/{id})
    //    - PUT  /users/update (body içerisinde id mevcutsa)
    // ========================================================
    @RequestMapping(value = {"/update/{id}", "/update"}, method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<?> updateUser(
            @PathVariable(required = false) Long id,
            @RequestBody User userDetails) {

        Long targetId = (id != null) ? id : userDetails.getId();
        if (targetId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Güncellenecek kullanıcı ID'si belirtilmelidir."));
        }

        try {
            User updatedUser = userService.updateUser(targetId, userDetails);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
