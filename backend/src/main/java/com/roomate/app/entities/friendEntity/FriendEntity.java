//package com.roomate.app.entities.friendEntity;
//
//import com.roomate.app.entities.UserEntity;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotNull;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//import java.util.Objects;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "friend")
//public class FriendEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "requesterId")
//    @NotNull
//    private UserEntity requester;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "addresseeId")
//    @NotNull
//    private UserEntity addressee;
//
//    @Enumerated(EnumType.STRING)
//    @NotNull
//    private FriendEnum status;
//
//    @NotNull
//    private LocalDateTime requestDate;
//
//    private LocalDateTime acceptedDate;
//
//    public FriendEntity() {
//        this.requestDate = LocalDateTime.now();
//    }
//
//    public FriendEntity(UserEntity requester, UserEntity addressee, FriendEnum status) {
//        this.requester = requester;
//        this.addressee = addressee;
//        this.status = status;
//        this.requestDate = LocalDateTime.now();
//        if (status == FriendEnum.ACCEPTED) {
//            this.acceptedDate = LocalDateTime.now();
//        }
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        FriendEntity that = (FriendEntity) o;
//        return Objects.equals(requester, that.requester) &&
//                Objects.equals(addressee, that.addressee) &&
//                Objects.equals(status, that.status);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(requester, addressee, status);
//    }
//
//    @Override
//    public String toString() {
//        return String.format("Friendship[id=%d, requesterId=%d, addresseeId=%d, status=%s, requestDate=%s]",
//                id, requester != null ? requester.getId() : null,
//                addressee != null ? addressee.getId() : null, status, requestDate);
//    }
//
//    public void accept() {
//        this.status = FriendEnum.ACCEPTED;
//    }
//
//    public void decline() {
//        this.status = FriendEnum.REJECTED;
//    }
//}
