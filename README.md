# flutter_xmpp
基于xmpp搭建openfire，实现IM功能。插件支持iOS、Android端。
## 使用说明
### 1、初始化
在使用其他方法之前，必须先初始化插件。
     
```
init(String hostName,String port,{String domin})
```
- hostName：服务端地址 （必传）
- port：服务端端口     （必传）
- domin：可选参数，domin是服务端域名，不传默认为hostName

### 2、登录

```
login(String userName,String password)
```
- userName：用户名
- password：用户密码

### 3、注册

```
register(String userName,String password)
```
- userName：用户名
- password：用户密码

### 4、发送消息
 
```
send({String type="text",@required String body,@required String to,Map args})
```

- type：消息类型，默认为text：文本消息
- body：消息内容
- to：消息接收者用户名
- args：扩展字段

### 5、断开连接

```
disconnect()
```

### 6、事件监听

```
addListener({Function onConnect,Function onLogin,Function onRegister,Function onReceiveMessage})
```
- onConnect：连接状态监听
- onLogin：登录状态监听
- onRegister：注册状态监听
- onReceiveMessage：接收消息监听