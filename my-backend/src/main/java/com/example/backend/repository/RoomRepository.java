package com.example.backend.repository;

import com.example.backend.domain.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE " +
           "(:hotelId IS NULL OR r.hotel.id = :hotelId) AND " +
           "(:roomType IS NULL OR r.roomType = :roomType) AND " +
           "(:status IS NULL OR r.status = :status)")
    Page<Room> findRoomsForAdmin(@Param("hotelId") Long hotelId,
                                @Param("roomType") String roomType,
                                @Param("status") String status,
                                Pageable pageable);

    List<Room> findByHotelId(Long hotelId);

    Integer countByHotelId(Long hotelId);

    Integer countByHotelIdAndStatus(Long hotelId, String status);

    @Query("SELECT CASE WHEN COUNT(DISTINCT inv.inventoryDate) > 0 THEN " +
           "(COUNT(DISTINCT res.id) * 100.0 / COUNT(DISTINCT inv.inventoryDate)) ELSE 0 END " +
           "FROM Room r " +
           "LEFT JOIN RoomInventory inv ON inv.room = r " +
           "LEFT JOIN Reservation res ON res.room = r " +
           "WHERE r.id = :roomId")
    Double getOccupancyRate(@Param("roomId") Long roomId);
}