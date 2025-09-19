package com.example.backend.repository;

import com.example.backend.domain.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    @Query("SELECT h.id, h.name, h.business.businessName, " +
           "COUNT(r), SUM(p.amount), AVG(rv.rating) " +
           "FROM Hotel h " +
           "LEFT JOIN Reservation r ON r.hotel = h " +
           "LEFT JOIN Payment p ON p.reservation = r AND p.status = 'PAID' " +
           "LEFT JOIN Review rv ON rv.hotel = h " +
           "WHERE r.createdAt BETWEEN :from AND :to " +
           "GROUP BY h.id, h.name, h.business.businessName " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> getTopHotelsByReservations(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to,
                                             org.springframework.data.domain.Pageable pageable);

    @Query("SELECT h FROM Hotel h WHERE " +
           "(:name IS NULL OR h.name LIKE %:name%) AND " +
           "(:status IS NULL OR h.status = :status) AND " +
           "(:city IS NULL OR h.address LIKE %:city%)")
    Page<Hotel> findHotelsForAdmin(@Param("name") String name,
                                  @Param("status") String status,
                                  @Param("city") String city,
                                  Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId")
    Double getAverageRating(@Param("hotelId") Long hotelId);
}