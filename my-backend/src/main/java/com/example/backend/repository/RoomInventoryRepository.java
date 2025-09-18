package com.example.backend.repository;

import com.example.backend.domain.RoomInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {

    List<RoomInventory> findByRoomId(Long roomId);

    List<RoomInventory> findByRoomIdOrderByInventoryDate(Long roomId);
}