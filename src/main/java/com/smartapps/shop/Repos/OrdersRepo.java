package com.smartapps.shop.Repos;

import com.smartapps.shop.Models.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepo extends JpaRepository<Orders, Long> {

}
