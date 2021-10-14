# 作用：模拟客户端，向flume_host:flume_port建立连接并发送数据file_path
# 启动参数：两个
#   - flume_host
#   - flume_port
#   - file_path
import requests
import time
import random
import sys
import getopt
import json

def generator(file_path):
    random.seed(19960106)
    with open(file_path,"r") as file:
        line = file.readline()
        while line:
            yield json.loads(line.strip("\n"))
            line = file.readline()
            time.sleep(random.randint(0,30)/10)

def getHostnamePort():
    argv = sys.argv[1:]
    try:
        opts, args = getopt.getopt(argv,"h:p:f:")
    except:
        print("Error")
        return None, None
    host_name, port = None, None
    for opt, arg in opts:
        if opt in ['-h']:
            host_name = arg
        elif opt in ['-p']:
            port = arg
        elif opt in ['-f']:
            file_path = arg
    return host_name, port, file_path

if __name__=="__main__":
    host_name, port, file_path = getHostnamePort()
    if (not host_name) or (not port) or (not file_path):
        print("Error2")
        exit(0)
    print(host_name, port)
    f = generator(file_path)
    while True:
        try:
            plain_data = next(f)
            print(plain_data)
            res = requests.post(url="http://"+host_name+":"+port, json=[plain_data])
        except StopIteration:
            break
    print("It's over!")


