# Java Redis Clone

This project is a simplified clone of Redis built in Java. It supports basic commands, key expiry, and several data structures like Lists and Hashes.

## How to Run

1.  **Prerequisites:** You must have a Java Development Kit (JDK) installed on your system.

2.  **Compilation:** Open a terminal or command prompt and navigate to the `src` directory within the project folder:
    ```sh
    cd C:\Users\jpc\poc-workspace\redis-clone\src
    ```
    Compile the Java source files:
    ```sh
    javac com/redisclone/RedisServer.java com/redisclone/ClientHandler.java com/redisclone/RedisObject.java
    ```

3.  **Execution:** From the same `src` directory, run the server:
    ```sh
    java com.redisclone.RedisServer
    ```
    The server will start and listen on the default Redis port, `6379`.

4.  **Connecting:** You can connect to the server using `redis-cli` or any other Redis client.
    ```sh
    redis-cli
    ```

## Usage Examples

Here are some examples of the supported commands.

### Basic Commands

```
127.0.0.1:6379> SET mykey "Hello, Redis!"
OK
127.0.0.1:6379> GET mykey
"Hello, Redis!"

127.0.0.1:6379> PING
PONG

127.0.0.1:6379> ECHO "Hello, World!"
"Hello, World!"
```

### Key Expiry

Set a key with a 10-second timeout:
```
127.0.0.1:6379> SETEX mykey 10 "Hello"
OK
```

Check the remaining time to live:
```
127.0.0.1:6379> TTL mykey
(integer) 9
```

Get the key before it expires:
```
127.0.0.1:6379> GET mykey
"Hello"
```

After 10 seconds, the key will be gone:
```
127.0.0.1:6379> GET mykey
(nil)
```

Set an expiration on an existing key:
```
127.0.0.1:6379> SET anotherkey "some value"
OK
127.0.0.1:6379> EXPIRE anotherkey 5
(integer) 1
127.0.0.1:6379> TTL anotherkey
(integer) 4
```

### Lists

Push elements to the left of a list:
```
127.0.0.1:6379> LPUSH mylist "world"
(integer) 1
127.0.0.1:6379> LPUSH mylist "hello"
(integer) 2
```

Get a range of elements from the list:
```
127.0.0.1:6379> LRANGE mylist 0 -1
1) "hello"
2) "world"
```

Pop an element from the right of the list:
```
127.0.0.1:6379> RPOP mylist
"world"
```

Pop an element from the left of the list:
```
127.0.0.1:6379> LPOP mylist
"hello"
```

### Hashes

Set field-value pairs in a hash:
```
127.0.0.1:6379> HSET myhash field1 "foo"
(integer) 1
```

Get a specific field from a hash:
```
127.0.0.1:6379> HGET myhash field1
"foo"
```

Set multiple fields at once:
```
127.0.0.1:6379> HSET myhash field2 "bar" field3 "baz"
(integer) 2
```

Get all fields and values from a hash:
```
127.0.0.1:6379> HGETALL myhash
1) "field1"
2) "foo"
3) "field2"
4) "bar"
5) "field3"
6) "baz"
```

Delete a field from a hash:
```
127.0.0.1:6379> HDEL myhash field2
(integer) 1
```

Check the hash again:
```
127.0.0.1:6379> HGETALL myhash
1) "field1"
2) "foo"
3) "field3"
4) "baz"
```
