package pk.edu.nu.isb.bms.models;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class MyUserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    public MyUser saveUser(MyUser user) {
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<MyUser> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            var userObj = user.get();
            return User.builder()
                    .username(userObj.getUsername())
                    .password(userObj.getPassword())
                    .disabled(!userObj.isEnabled())
                    .accountLocked(userObj.isAccountLocked())
                    .roles("USER")
                    .build();
        }
        throw new UsernameNotFoundException(username);
    }
}
