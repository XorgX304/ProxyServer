#!/usr/bin/env bash

openssl genrsa -out privateKey.pem 2048

openssl pkcs8 -topk8 -in privateKey.pem -out privateKey.pkcs8.pem -nocrypt

#生成配对的公钥
#openssl rsa -in privateKey.pem -out publicKey.pem -pubout

#创建CSR
#openssl req -new -key privateKey.pem  -out csr.pem
#显示CSR内容
#openssl req -in csr.pem -text

#生成自签名的证书
#openssl req -in csr.pem -key privateKey.pem -x509 -days 365 -out cer.pem