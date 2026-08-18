package com.dev.order_fulfillment.auth;

import com.dev.order_fulfillment.auth.dto.AuthResponse;
import com.dev.order_fulfillment.auth.dto.LoginRequest;
import com.dev.order_fulfillment.auth.dto.RegisterRequest;
import com.dev.order_fulfillment.auth.jwt.JwtUtil;
import com.dev.order_fulfillment.customer.Customer;
import com.dev.order_fulfillment.customer.CustomerRepository;
import com.dev.order_fulfillment.customer.Role;
import com.dev.order_fulfillment.exceptionHandling.ConflictException;
import com.dev.order_fulfillment.exceptionHandling.UnauthorizedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    public AuthService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request){

        if (customerRepository.existsByEmail(request.email())){
            throw new ConflictException("Email already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        Customer customer = Customer.builder()
                .username(request.username())
                .password(hashedPassword)
                .email(request.email())
                .role(Role.CUSTOMER)
                .build();

        customerRepository.save(customer);

        String token = jwtUtil.generateToken(customer.getUsername());

        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request){

        UserDetails userDetails = loadUserByUsername(request.username());

        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())){
            throw new UnauthorizedException("Username or password incorrect");
        }

        String token = jwtUtil.generateToken(request.username());
        return new AuthResponse(token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return customerRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Username or password incoorect"));
    }
}
