//package com.roomate.app.repository;
//
//import com.roomate.app.entities.friendEntity.FriendEntity;
//import com.roomate.app.entities.friendEntity.FriendEnum;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface FriendRepository extends JpaRepository<FriendEntity, Long> {
//
//    @Query("SELECT f FROM FriendEntity f WHERE " +
//            "(f.requester.id = :userId OR f.addressee.id = :userId) " +
//            "AND f.status = :status")
//    List<FriendEntity> findByUserIdAndStatus(@Param("userId") Long userId,
//                                             @Param("status") FriendEnum status);
//
//    @Query("SELECT f FROM FriendEntity f WHERE " +
//            "f.requester.id = :requesterId AND f.addressee.id = :addresseeId")
//    Optional<FriendEntity> findByRequesterAndAddressee(@Param("requesterId") Long requesterId,
//                                                       @Param("addresseeId") Long addresseeId);
//
//    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FriendEntity f WHERE " +
//            "((f.requester.id = :user1Id AND f.addressee.id = :user2Id) OR " +
//            "(f.requester.id = :user2Id AND f.addressee.id = :user1Id)) " +
//            "AND f.status = 'ACCEPTED'")
//    boolean areFriends(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);
//}
