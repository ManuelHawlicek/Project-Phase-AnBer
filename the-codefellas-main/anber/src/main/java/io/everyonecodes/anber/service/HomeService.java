package io.everyonecodes.anber.service;

import io.everyonecodes.anber.data.Home;
import io.everyonecodes.anber.data.HomeType;
import io.everyonecodes.anber.data.User;
import io.everyonecodes.anber.repository.HomeRepository;
import io.everyonecodes.anber.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HomeService {

    private final HomeRepository homeRepository;
    private final UserRepository userRepository;
    private final List<String> homeProperties;

    public HomeService(HomeRepository homeRepository, UserRepository userRepository, List<String> homeProperties) {
        this.homeRepository = homeRepository;
        this.userRepository = userRepository;
        this.homeProperties = homeProperties;
    }

    public List<Home> getHomes(String username) {

        Optional<User> oProfile = userRepository.findOneByEmail(username);
        if (oProfile.isPresent()) {
            User profile = oProfile.get();
            return profile.getSavedHomes();
        } else {
            return List.of();
        }
    }

    public List<Home> addHome(Home home, String username) {
        var oProfile = userRepository.findOneByEmail(username);
        List<Home> homes = new ArrayList<>();
        if (oProfile.isPresent()) {
            User profile = oProfile.get();
            homes = profile.getSavedHomes();
            if (!homes.contains(home)) {
                homes.add(home);
                profile.setSavedHomes(homes);
                homeRepository.save(home);
                userRepository.save(profile);
            }
        }
        return homes;
    }

    public Optional<Home> editHome(String username,  Long id, String property,String input) {

        Optional<User> oUser = userRepository.findOneByEmail(username);
        if (oUser.isPresent()){
            User user = oUser.get();
            Optional<Home> oHomeToEdit = user.getSavedHomes().stream()
                    .filter(home -> home.getId().equals(id))
                    .findFirst();

            if (oHomeToEdit.isPresent()){
                Home homeToEdit = oHomeToEdit.get();
                overwriteData(property, homeToEdit, input);
                List<Home> userHomes = user.getSavedHomes();

                int indexHome = userHomes.indexOf(homeToEdit);

                userHomes.set(indexHome, homeToEdit);
                userRepository.save(user);
                return Optional.of(homeToEdit);
            }
        }
        return Optional.empty();
    }

    public void removeHome(String username,  Long id) {

        Optional<User> oUser = userRepository.findOneByEmail(username);
        if (oUser.isPresent()){
            User user = oUser.get();
            Home homeToRemove = user.getSavedHomes().stream()
                    .filter(home -> home.getId().equals(id))
                    .findFirst().orElse(null);

            List<Home> userHomes = user.getSavedHomes();
            userHomes.remove(homeToRemove);
            userRepository.save(user);
            Long homeId = homeToRemove.getId();
            homeRepository.deleteById(homeId);
        }
    }

    public void deleteAllHomes(String username) {

        Optional<User> oUser = userRepository.findOneByEmail(username);
        if (oUser.isPresent()){
            User user = oUser.get();

            List<Home> userHomes = user.getSavedHomes();

            List<Long> ids = userHomes.stream()
                    .map(Home::getId)
                    .collect(Collectors.toList());

            userHomes.clear();

            userRepository.save(user);
            homeRepository.deleteAllByIdInBatch(ids);

        }
    }

    private void overwriteData(String property, Home home, String input) {

        if (homeProperties.contains(property)) {
            if (property.equals(homeProperties.get(0))) {
                home.setHomeName(input);
            }
            if (property.equals(homeProperties.get(1))) {
                home.setCountry(input);
            }
            if (property.equals(homeProperties.get(2))) {
                home.setCity(input);
            }
            if (property.equals(homeProperties.get(3))) {
                home.setPostalCode(input);
            }
            if (property.equals(homeProperties.get(4))) {
                home.setType(HomeType.valueOf(input.toUpperCase()));
            }

            if (property.equals(homeProperties.get(5))) {
                home.setSizeInSquareMeters(Double.parseDouble(input));
            }
            homeRepository.save(home);
        }
    }
}
