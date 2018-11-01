# 项目介绍
基于HBase的对象存储服务 (HBase-object-store-service)
使用spring boot 2.0.2.RELEASE版本
使用mysql 5.7.21 HBase 1.2.6
构建工具使用 Gradle 4.6

## 模块说明
oss-common         公共类模块, server模块以及sdk模块的依赖模块 
oss-core           用户管理及权限管理模块  
oss-server         文件管理模块,包含bucket管理和文件管理  
oss-api            REST API 接口服务模块  
oss-sdk            sdk模块  WebClient  
script             数据库mysql脚本  

### 软件架构
软件架构说明


#### 创建用户

1. 在用户表中创建用户信息
2. 在token表中插入一条TOKEN信息，token的id为用户id，并保证token不会过期

#### 删除用户

1. 删除用户表的用户数据
2. 删除用户的token以及所有的授权

#### 删除token

1. 删除token表中的基本信息
2. 需要删除token对应的所有授权

#### 创建bucket

1. 在bucket表中插入bucket基本信息
2. 授权表中插入对bucket的授权信息-当前登录人
3. 在HBase中进行创建 目录bucket，文件bucket两张表
4. 创建完成bucket 后，需要将bucket name作为row key 插入到 seqid 表中，值设为0

#### 删除bucket

1. 删除bucket表中的bucket信息
2. 删除授权表中的授权信息
3. 删除HBase中 目录bucket
4. 删除HBase中 文件bucket
5. 删除HBase中 seqId表中对应bucket的信息

