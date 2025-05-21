package com.post.service;

import com.post.dto.KafkaBanDto;
import com.post.dto.KafkaUserDto;
import com.post.entity.User;
import com.post.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics="updateUser", groupId = "eum-community")
    public void updateUser(String message){
        KafkaUserDto kafkaUserDto;
        try{
            kafkaUserDto = objectMapper.readValue(message ,KafkaUserDto.class);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        User user = userRepository.findById(kafkaUserDto.getUserId()).get();
        user.setName(kafkaUserDto.getName());
        user.setNation(kafkaUserDto.getNation());
        user.setLanguage(kafkaUserDto.getLanguage());
        user.setRole(kafkaUserDto.getRole());
        user.setAddress(kafkaUserDto.getAddress());
        userRepository.save(user);
    }

    @KafkaListener(topics="updateLanguage", groupId = "eum-community")
    public void updateLanguage(String message){
        KafkaUserDto kafkaUserDto;
        try{
            kafkaUserDto = objectMapper.readValue(message ,KafkaUserDto.class);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        User user = userRepository.findById(kafkaUserDto.getUserId()).get();
        user.setLanguage(kafkaUserDto.getLanguage());
        userRepository.save(user);
    }

    @KafkaListener(topics="createUser", groupId = "eum-community")
    public void createUser(String message) {
        KafkaUserDto kafkaUserDto;
        try {
            kafkaUserDto = objectMapper.readValue(message, KafkaUserDto.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        User user = User.builder()
                .name(kafkaUserDto.getName())
                .nation(kafkaUserDto.getNation())
                .language(kafkaUserDto.getLanguage())
                .role(kafkaUserDto.getRole())
                .address(kafkaUserDto.getAddress())
                .userId(kafkaUserDto.getUserId())
                .ban(0)
                .build();
        userRepository.save(user);
    }

    @Transactional
    @KafkaListener(topics="deleteUser", groupId = "eum-community")
    public void deleteUser(String message){
        KafkaUserDto kafkaUserDto;
        try{
            kafkaUserDto = objectMapper.readValue(message ,KafkaUserDto.class);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        User user = userRepository.findById(kafkaUserDto.getUserId()).get();
        userRepository.delete(user);
    }

    @KafkaListener(topics="deactivate", groupId = "eum-community")
    public void updateBan(String message){
        KafkaBanDto kafkaBanDto;
        try{
            kafkaBanDto = objectMapper.readValue(message, KafkaBanDto.class);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        User user = userRepository.findById(kafkaBanDto.getUserId()).get();
        user.setBan(kafkaBanDto.getDeactivate());
        userRepository.save(user);
    }
}
