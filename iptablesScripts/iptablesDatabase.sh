#!/bin/bash

sudo iptables -F
sudo iptables -A INPUT -p tcp -s 192.168.1.254 --dport 5432 -d 192.168.1.1 -j ACCEPT
sudo iptables -A INPUT -j REJECT

sudo iptables -A OUTPUT -p tcp -s 192.168.1.1 --sport 5432 -d 192.168.1.254 -m state --state ESTABLISHED,RELATED -j ACCEPT
sudo iptables -A OUTPUT -j REJECT