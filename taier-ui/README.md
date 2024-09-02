# Taier-ui

The frontEnd projects of Taier.

## Build
Please go to the Taier-ui directory:
错误处理：
code: 'ERR_OSSL_EVP_UNSUPPORTED'
使用 --openssl-legacy-provider
   在构建命令前设置环境变量 NODE_OPTIONS，以使用旧版的 OpenSSL 提供程序：

```
export NODE_OPTIONS=--openssl-legacy-provider
pnpm run build
或者在 package.json 中的构建脚本中直接指定：
{
  "scripts": {
    "build": "NODE_OPTIONS=--openssl-legacy-provider umi build"
  }
}

```


```bash
yarn build
```

```bash
pnpm install  
pnpm run build
```


or

```bash
mvn clean package
```

## Running

```bash
yarn start
```
