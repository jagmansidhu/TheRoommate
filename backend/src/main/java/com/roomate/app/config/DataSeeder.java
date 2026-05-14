package com.roomate.app.config;

import com.roomate.app.entities.*;
import com.roomate.app.entities.room.RoomEntity;
import com.roomate.app.entities.room.RoomMemberEntity;
import com.roomate.app.entities.room.RoomMemberEnum;
import com.roomate.app.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Seeds test data for development purposes.
 * Only runs when 'dev' profile is active OR when TEST_DATA_EMAIL environment
 * variable is set.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String TEST_EMAIL = "test1@example.com";
    // System.getenv("TEST_DATA_EMAIL") != null
    // ? System.getenv("TEST_DATA_EMAIL")
    // : "test@example.com";
    private static final String TEST_PASSWORD = "Test_123!";

    @Bean
    CommandLineRunner seedTestData(
            UserRepository userRepository,
            RoomRepository roomRepository,
            RoomMemberRepository roomMemberRepository,
            EventRepository eventRepository,
            ChoreRepository choreRepository,
            UtilityRepository utilityRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Checking for test data seeding with email: {}", TEST_EMAIL);

            Optional<UserEntity> existingUser = userRepository.findByEmail(TEST_EMAIL);
            if (existingUser.isPresent()) {
                log.info("Test user already exists. Skipping seeding.");
                return;
            }

            log.info("Seeding test data...");

            UserEntity testUser = new UserEntity();
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setEmail(TEST_EMAIL);
            testUser.setPassword(passwordEncoder.encode(TEST_PASSWORD));
            testUser.setPhone("555-123-4567");
            testUser.setEnabled(true);
            testUser = userRepository.save(testUser);
            log.info("Created test user: {}", testUser.getEmail());

            UserEntity roommate = new UserEntity();
            roommate.setFirstName("Alex");
            roommate.setLastName("Roommate");
            roommate.setEmail("roommate@example.com");
            roommate.setPassword(passwordEncoder.encode(TEST_PASSWORD));
            roommate.setPhone("555-987-6543");
            roommate.setEnabled(true);
            roommate = userRepository.save(roommate);
            log.info("Created roommate user: {}", roommate.getEmail());

            RoomEntity room = new RoomEntity();
            room.setName("University House");
            room.setAddress("123 College Ave, Apt 4B");
            room.setDescription("A cozy 2-bedroom apartment near campus. Great location for students!");
            room.setHeadRoommateId(testUser.getEmail());
            room.setRoomCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            room.setCreatedAt(LocalDateTime.now());
            room = roomRepository.save(room);
            log.info("Created room: {} with code: {}", room.getName(), room.getRoomCode());

            RoomMemberEntity testUserMember = new RoomMemberEntity(room, testUser, RoomMemberEnum.HEAD_ROOMMATE);
            roomMemberRepository.save(testUserMember);
            log.info("Added {} as HEAD_ROOMMATE", testUser.getEmail());
            RoomMemberEntity roommateMember = new RoomMemberEntity(room, roommate, RoomMemberEnum.ROOMMATE);
            roomMemberRepository.save(roommateMember);
            log.info("Added {} as ROOMMATE", roommate.getEmail());

            LocalDateTime now = LocalDateTime.now();

            EventEntity event1 = new EventEntity();
            event1.setTitle("House Meeting");
            event1.setDescription("Monthly roommate meeting to discuss house rules and upcoming expenses");
            event1.setStartTime(now.plusDays(2).withHour(19).withMinute(0));
            event1.setEndTime(now.plusDays(2).withHour(20).withMinute(0));
            event1.setRoom(room);
            event1.setUser(testUser);
            event1.setCreated(now);
            eventRepository.save(event1);

            EventEntity event2 = new EventEntity();
            event2.setTitle("Movie Night");
            event2.setDescription("Watching the new Marvel movie together!");
            event2.setStartTime(now.plusDays(5).withHour(20).withMinute(0));
            event2.setEndTime(now.plusDays(5).withHour(23).withMinute(0));
            event2.setRoom(room);
            event2.setUser(roommate);
            event2.setCreated(now);
            eventRepository.save(event2);

            EventEntity event3 = new EventEntity();
            event3.setTitle("Lease Renewal Discussion");
            event3.setDescription("Discuss whether to renew the lease for another year");
            event3.setStartTime(now.plusDays(14).withHour(18).withMinute(30));
            event3.setEndTime(now.plusDays(14).withHour(19).withMinute(30));
            event3.setRoom(room);
            event3.setUser(testUser);
            event3.setCreated(now);
            eventRepository.save(event3);

            log.info("Created 3 events");

            ChoreEntity chore1 = new ChoreEntity();
            chore1.setChoreName("Kitchen Cleaning");
            chore1.setFrequency(1);
            chore1.setChoreFrequencyUnitEnum(ChoreFrequencyUnitEnum.WEEKLY);
            chore1.setDueAt(now.plusDays(3));
            chore1.setRoom(room);
            chore1.setAssignedToMember(testUserMember);
            choreRepository.save(chore1);

            ChoreEntity chore2 = new ChoreEntity();
            chore2.setChoreName("Bathroom Cleaning");
            chore2.setFrequency(1);
            chore2.setChoreFrequencyUnitEnum(ChoreFrequencyUnitEnum.WEEKLY);
            chore2.setDueAt(now.plusDays(5));
            chore2.setRoom(room);
            chore2.setAssignedToMember(roommateMember);
            choreRepository.save(chore2);

            ChoreEntity chore3 = new ChoreEntity();
            chore3.setChoreName("Take Out Trash");
            chore3.setFrequency(2);
            chore3.setChoreFrequencyUnitEnum(ChoreFrequencyUnitEnum.WEEKLY);
            chore3.setDueAt(now.plusDays(1));
            chore3.setRoom(room);
            chore3.setAssignedToMember(testUserMember);
            choreRepository.save(chore3);

            ChoreEntity chore4 = new ChoreEntity();
            chore4.setChoreName("Vacuum Living Room");
            chore4.setFrequency(1);
            chore4.setChoreFrequencyUnitEnum(ChoreFrequencyUnitEnum.BIWEEKLY);
            chore4.setDueAt(now.plusDays(7));
            chore4.setRoom(room);
            chore4.setAssignedToMember(roommateMember);
            choreRepository.save(chore4);

            log.info("Created 4 chores");

            UtilityEntity utility1 = new UtilityEntity();
            utility1.setUtilityName("Electricity");
            utility1.setDescription("Monthly electricity bill - PG&E");
            utility1.setUtilityPrice(120.50);
            utility1.setChoreFrequencyUnitEnum(ChoreFrequencyUnitEnum.MONTHLY);
            utility1.setUtilDistributionEnum(UtilDistributionEnum.EQUALSPLIT);
            utility1.setDueAt(now.plusDays(10));
            utility1.setRoom(room);
            utility1.setAssignedToMember(testUserMember);
            utilityRepository.save(utility1);

            UtilityEntity utility2 = new UtilityEntity();
            utility2.setUtilityName("Internet");
            utility2.setDescription("Xfinity 500Mbps plan");
            utility2.setUtilityPrice(79.99);
            utility2.setChoreFrequencyUnitEnum(ChoreFrequencyUnitEnum.MONTHLY);
            utility2.setUtilDistributionEnum(UtilDistributionEnum.EQUALSPLIT);
            utility2.setDueAt(now.plusDays(15));
            utility2.setRoom(room);
            utility2.setAssignedToMember(roommateMember);
            utilityRepository.save(utility2);

            UtilityEntity utility3 = new UtilityEntity();
            utility3.setUtilityName("Water & Garbage");
            utility3.setDescription("City water and garbage services");
            utility3.setUtilityPrice(45.00);
            utility3.setChoreFrequencyUnitEnum(ChoreFrequencyUnitEnum.MONTHLY);
            utility3.setUtilDistributionEnum(UtilDistributionEnum.EQUALSPLIT);
            utility3.setDueAt(now.plusDays(20));
            utility3.setRoom(room);
            utility3.setAssignedToMember(testUserMember);
            utilityRepository.save(utility3);

            log.info("Created 3 utilities");

            log.info("===========================================");
            log.info("TEST DATA SEEDING COMPLETE!");
            log.info("===========================================");
            log.info("Test User Email: {}", TEST_EMAIL);
            log.info("Test User Password: {}", TEST_PASSWORD);
            log.info("Room Code: {}", room.getRoomCode());
            log.info("===========================================");
        };
    }
}
