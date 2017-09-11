# ProxyServer

## Destination illustration
场景介绍:项目中调用第三方功能,但是只能在服务器中收到回调通知,此时在本地机器调试时,是无法收到回调通知的,给开发调试带来了困难。
该程序的实现目标就是把服务器的回调通知导向到本地,从而为调试带来便利。

## Design illustration
使用netty开发TCP协议。原理上也很简单,先在本地创建一个连接远程服务器的连接,然后当服务器收到回调通知时,通过这个连接流向本地程序。

### Server illustration
ServerBootstrap是server端的启动类。
serverListenerPort:
channelListenerPort:

### Client illustration
ClientBootstrap是客户端的启动类。
localhost:
localPort:
remoteHost:
remotePort:

### Security
使用TLS对客户端进行验证,只有被授权的客户端才被允许连接服务器。