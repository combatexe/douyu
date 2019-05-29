# 斗鱼弹幕抓取程序
斗鱼弹幕连接客户端是一个快速连接斗鱼弹幕服务器，获取指定主播房间弹幕、送礼等消息的应用程序。
# 应用介绍
1. douyu-front应用为展示斗鱼弹幕分析数据。
2. douyu-admin应用为管理弹幕连接服务器。
# 安装所需依赖
1. 安装jdk,最低版本1.8
2. Elasticsearch组件，版本为6.2.4
3. 安装mysql
# 打包
1. mvn install
2. mvn package -Dmaven.test.skip=true
# 运行
