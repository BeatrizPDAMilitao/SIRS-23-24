#!/bin/bash

sudo sysctl net.ipv4.ip_forward=1
sudo iptables -F
sudo iptables -A FORWARD -p tcp -s 192.168.0.100 --sport 5000 -m state --state RELATED,ESTABLISHED -j ACCEPT
sudo iptables -A FORWARD -p tcp -d 192.168.0.100 --dport 5000 -j ACCEPT
sudo iptables -A INPUT -j REJECT
sudo iptables -A OUTPUT -j REJECT
sudo iptables -A FORWARD -j REJECT
