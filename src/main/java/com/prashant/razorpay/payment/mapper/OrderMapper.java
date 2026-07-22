package com.prashant.razorpay.payment.mapper;

import com.prashant.razorpay.payment.dto.response.OrderResponse;
import com.prashant.razorpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toResponse(OrderRecord orderRecord);
}
