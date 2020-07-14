package com.xuliang.grpc.facade;


import com.xuliang.grpc.entity.UserEntity;
import com.xuliang.grpc.starter.annotation.GrpcService;
import com.xuliang.grpc.starter.constants.SerializeType;

import java.util.List;

@GrpcService(server = "user", serialization = SerializeType.PROTOSTUFF)
public interface UserService {

    void insert(UserEntity userEntity);

    void deleteById(Long id);

    List<UserEntity> findAll();

}
