package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity=userRepository.findByEmail(username);
        if (userEntity==null){
            throw new UsernameNotFoundException(username);
        }
        return new User(userEntity.getEmail(), userEntity.getEncryptedPwd(),
                true,true,true,true,new ArrayList<>());

    }

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    private final Environment env;

    private final RestTemplate restTemplate;

    private final OrderServiceClient orderServiceClient;

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());
        ModelMapper mapper=new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity=mapper.map(userDto,UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        userRepository.save(userEntity);
        UserDto returenDto= mapper.map(userEntity,UserDto.class);
        return returenDto;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity=userRepository.findByUserId(userId);
        if (userEntity==null){
            throw new UsernameNotFoundException("User not found");
        }
        UserDto userDto= new ModelMapper().map(userEntity,UserDto.class);
//        List<ResponseOrder> orders = new ArrayList<>();
        /**
         * Using as rest template
         */
        //String orderUrl="http://127.0.0.1:8000/order-service/%s/orders";
//        String orderUrl= String.format(env.getProperty("order_service.url"),userId);
//        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<ResponseOrder>>() {
//        });
//        List<ResponseOrder> orderList = orderListResponse.getBody();
        List<ResponseOrder> ordersList= orderServiceClient.getOrders(userId);

        userDto.setOrders(ordersList);
        return userDto;


    }

    @Override
    public Iterable<UserEntity> getUserByAll() {
        return userRepository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity==null){
            throw new UsernameNotFoundException(email);
        }
        UserDto u = new ModelMapper().map(userEntity, UserDto.class);
        return u;

    }


}
