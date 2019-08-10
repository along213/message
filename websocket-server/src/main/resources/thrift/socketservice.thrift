namespace java com.allqj.ajf.thrift_test
# 这是经过泛化后的Apache Thrift接口
service ISendMessageService {
    SocketResponse send(1:required SocketRequest request);
}

struct SocketRequest {
    # 传递的参数信息，使用格式进行表示
    1:required string message;
    # 服务调用者发送目标的服务id
    2:required string projectId;
    # 服务调用者请求的组名
    3:optional string group;
    # 服务调用者请求的key
    4:optional string key;
    #是否开启失败回调
    5:optional bool failureCallback = false;
}
struct SocketResponse {
    # 传递的参数信息，使用格式进行表示
    1:optional string message;
    # 服务调用者发送目标的服务id
    2:required bool resultType;
}