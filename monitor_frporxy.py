# -*- coding: utf-8 -*-
#!/usr/bin/env python

import os
import subprocess
import time


class GetProcess:

    def GetCount(_self,):
         try:
            _self = subprocess.Popen('/usr/sbin/ss dport = :2181  | grep -v State || /bin/netstat -naltp | grep frproxy | grep 2181', stdout=subprocess.PIPE, shell=True)
            return len(_self.stdout.readlines())
         except SyntaxError:
             return 0
    def GetProcessCount(_self,):
         try:
            _self = subprocess.Popen('ps -ef | grep frproxy | grep -v grep | grep -v "monitor_frproxy.py"', stdout=subprocess.PIPE, shell=True)
            return len(_self.stdout.readlines())
         except SyntaxError:
             return 0

a = GetProcess()

if a.GetProcessCount() is not None:
    if a.GetProcessCount() < 1:
        os.system('pkill frproxy;/etc/init.d/frproxy restart &')
        time.sleep(5)
        if a.GetProcessCount() < 1:
            print("Starting frproxy failed,Sending email & sms to ops")
            os.system('curl "http://ms.web.elenet.net/sms/warn?function=froproxy_is_dead&ms=$(hostname)&mo=18610630721,18500974097,18210085607&priority=Z"')
            os.system('echo "frproxy of  $(hostname) is dead!" | mail -s "frproxy of $(hostname) is dead" zangbaocheng@ele.to yangchunchao@ele.to yuanshuai@ele.to')
    else:
        print("frproxy is running,checking 2181")
        if a.GetCount() is not None:
            if a.GetCount() < 1:
                print('Checking 2181 fail')
                os.system('pkill frproxy;/etc/init.d/frproxy restart &')
                time.sleep(5)
                if a.GetCount() < 1:
                    print("Starting frproxy failed,Sending email & sms to ops")
                    os.system('curl "http://ms.web.elenet.net/sms/warn?function=froproxy_is_dead&ms=$(hostname)&mo=18610630721,18500974097,18210085607&priority=Z"')
                    os.system('echo "frproxy of  $(hostname) is dead!!" | mail -s "frproxy of $(hostname) is dead" zangbaocheng@ele.to yangchunchao@ele.to yuanshuai@ele.to')
