package com.redisclone.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

import com.redisclone.model.ObjectType;
import com.redisclone.model.RedisObject;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ConcurrentHashMap<String, RedisObject> dataStore;

    public ClientHandler(Socket socket, ConcurrentHashMap<String, RedisObject> dataStore) {
        this.clientSocket = socket;
        this.dataStore = dataStore;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                String[] tokens = inputLine.split("\\s+");
                String command = tokens[0].toUpperCase();

                switch (command) {
                    case "SET":
                        if (tokens.length == 3) {
                            dataStore.put(tokens[1], new RedisObject(ObjectType.STRING, tokens[2]));
                            out.println("OK");
                        } else {
                            out.println("(error) ERR wrong number of arguments for 'set' command");
                        }
                        break;
                    case "GET":
                        if (tokens.length == 2) {
                            RedisObject obj = dataStore.get(tokens[1]);
                            if (obj != null && !obj.isExpired()) {
                                if (obj.getType() == ObjectType.STRING) {
                                	out.println("\"" + obj.getValue() + "\"");                                } else {
                                    out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
                                }
                            } else {
                                out.println("(nil)");
                            }
                        }
                        break;
                    case "SETEX":
                        if (tokens.length == 4) {
                            try {
                                long seconds = Long.parseLong(tokens[2]);
                                RedisObject obj = new RedisObject(ObjectType.STRING, tokens[3]);
                                obj.setExpireAt(System.currentTimeMillis() + seconds * 1000);
                                dataStore.put(tokens[1], obj);
                                out.println("OK");
                            } catch (NumberFormatException e) {
                                out.println("(error) ERR value is not an integer or out of range");
                            }
                        } else {
                            out.println("(error) ERR wrong number of arguments for 'setex' command");
                        }
                        break;
                    case "EXPIRE":
                        if (tokens.length == 3) {
                            RedisObject obj = dataStore.get(tokens[1]);
                            if (obj != null) {
                                try {
                                    long seconds = Long.parseLong(tokens[2]);
                                    obj.setExpireAt(System.currentTimeMillis() + seconds * 1000);
                                    out.println("(integer) 1");
                                } catch (NumberFormatException e) {
                                    out.println("(error) ERR value is not an integer or out of range");
                                }
                            } else {
                                out.println("(integer) 0");
                            }
                        } else {
                            out.println("(error) ERR wrong number of arguments for 'expire' command");
                        }
                        break;
                    case "TTL":
                        if (tokens.length == 2) {
                            RedisObject obj = dataStore.get(tokens[1]);
                            if (obj != null) {
                                if (obj.isExpired()) {
                                    out.println("(integer) -2");
                                } else if (obj.getExpireAt() == -1) {
                                    out.println("(integer) -1");
                                } else {
                                    long ttl = (obj.getExpireAt() - System.currentTimeMillis()) / 1000;
                                    out.println("(integer) " + ttl);
                                }
                            } else {
                                out.println("(integer) -2");
                            }
                        }
                        break;
                    case "LPUSH":
                        handleListPush(tokens, out, true);
                        break;
                    case "RPUSH":
                        handleListPush(tokens, out, false);
                        break;
                    case "LPOP":
                        handleListPop(tokens, out, true);
                        break;
                    case "RPOP":
                        handleListPop(tokens, out, false);
                        break;
                    case "LRANGE":
                        handleListRange(tokens, out);
                        break;
                    case "HSET":
                        handleHashSet(tokens, out);
                        break;
                    case "HGET":
                        handleHashGet(tokens, out);
                        break;
                    case "HGETALL":
                        handleHashGetAll(tokens, out);
                        break;
                    case "HDEL":
                        handleHashDel(tokens, out);
                        break;
                    case "PING":
                        out.println("PONG");
                        break;
                    case "ECHO":
                        if (tokens.length == 2) {
                            out.println("\"" + tokens[1] + "\"");
                        } else {
                            out.println("(error) ERR wrong number of arguments for 'echo' command");
                        }
                        break;
                    default:
                        out.println("(error) ERR unknown command '" + command + "'");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleListPush(String[] tokens, PrintWriter out, boolean left) {
        if (tokens.length < 3) {
            out.println("(error) ERR wrong number of arguments for '" + tokens[0].toLowerCase() + "' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj != null && obj.getType() != ObjectType.LIST) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        if (obj == null) {
            obj = new RedisObject(ObjectType.LIST, new java.util.LinkedList<String>());
            dataStore.put(tokens[1], obj);
        }
        java.util.List<String> list = obj.getValue();
        for (int i = 2; i < tokens.length; i++) {
            if (left) {
                list.add(0, tokens[i]);
            } else {
                list.add(tokens[i]);
            }
        }
        out.println("(integer) " + list.size());
    }

    private void handleListPop(String[] tokens, PrintWriter out, boolean left) {
        if (tokens.length != 2) {
            out.println("(error) ERR wrong number of arguments for '" + tokens[0].toLowerCase() + "' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj == null) {
            out.println("(nil)");
            return;
        }
        if (obj.getType() != ObjectType.LIST) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        java.util.List<String> list = obj.getValue();
        if (list.isEmpty()) {
            out.println("(nil)");
        } else {
            String value = left ? list.remove(0) : list.remove(list.size() - 1);
            out.println("\"" + value + "\"");
        }
    }

    private void handleListRange(String[] tokens, PrintWriter out) {
        if (tokens.length != 4) {
            out.println("(error) ERR wrong number of arguments for 'lrange' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj == null) {
            out.println("(empty list or set)");
            return;
        }
        if (obj.getType() != ObjectType.LIST) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        java.util.List<String> list = obj.getValue();
        try {
            int start = Integer.parseInt(tokens[2]);
            int end = Integer.parseInt(tokens[3]);
            if (start < 0) start = list.size() + start;
            if (end < 0) end = list.size() + end;
            if (start < 0) start = 0;
            if (end >= list.size()) end = list.size() - 1;

            if (start > end) {
                out.println("(empty list or set)");
                return;
            }

            for (int i = start; i <= end; i++) {
                out.println((i - start + 1) + ") \"" + list.get(i) + "\"");
            }
        } catch (NumberFormatException e) {
            out.println("(error) ERR value is not an integer or out of range");
        }
    }

    private void handleHashSet(String[] tokens, PrintWriter out) {
        if (tokens.length < 4 || tokens.length % 2 != 0) {
            out.println("(error) ERR wrong number of arguments for 'hset' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj != null && obj.getType() != ObjectType.HASH) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        if (obj == null) {
            obj = new RedisObject(ObjectType.HASH, new java.util.concurrent.ConcurrentHashMap<String, String>());
            dataStore.put(tokens[1], obj);
        }
        java.util.Map<String, String> hash = obj.getValue();
        int newFields = 0;
        for (int i = 2; i < tokens.length; i += 2) {
            if (hash.put(tokens[i], tokens[i + 1]) == null) {
                newFields++;
            }
        }
        out.println("(integer) " + newFields);
    }

    private void handleHashGet(String[] tokens, PrintWriter out) {
        if (tokens.length != 3) {
            out.println("(error) ERR wrong number of arguments for 'hget' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj == null) {
            out.println("(nil)");
            return;
        }
        if (obj.getType() != ObjectType.HASH) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        java.util.Map<String, String> hash = obj.getValue();
        String value = hash.get(tokens[2]);
        if (value != null) {
            out.println("\"" + value + "\"");
        } else {
            out.println("(nil)");
        }
    }

    private void handleHashGetAll(String[] tokens, PrintWriter out) {
        if (tokens.length != 2) {
            out.println("(error) ERR wrong number of arguments for 'hgetall' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj == null) {
            out.println("(empty list or set)");
            return;
        }
        if (obj.getType() != ObjectType.HASH) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        java.util.Map<String, String> hash = obj.getValue();
        int i = 1;
        for (java.util.Map.Entry<String, String> entry : hash.entrySet()) {
            out.println(i++ + ") \"" + entry.getKey() + "\"");
            out.println(i++ + ") \"" + entry.getValue() + "\"");
        }
    }

    private void handleHashDel(String[] tokens, PrintWriter out) {
        if (tokens.length < 3) {
            out.println("(error) ERR wrong number of arguments for 'hdel' command");
            return;
        }
        RedisObject obj = dataStore.get(tokens[1]);
        if (obj == null) {
            out.println("(integer) 0");
            return;
        }
        if (obj.getType() != ObjectType.HASH) {
            out.println("(error) WRONGTYPE Operation against a key holding the wrong kind of value");
            return;
        }
        java.util.Map<String, String> hash = obj.getValue();
        int deletedFields = 0;
        for (int i = 2; i < tokens.length; i++) {
            if (hash.remove(tokens[i]) != null) {
                deletedFields++;
            }
        }
        out.println("(integer) " + deletedFields);
    }
}
