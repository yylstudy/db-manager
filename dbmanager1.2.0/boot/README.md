增加k8s数据清理功能 



ssh到目标数据库，安装nfs客户端
yum -y install nfs-utils
创建用户
adduser dbmanager
设置密码
passwd dbmanager
安装xtrabackup
yum install -y percona-xtrabackup-24-2.4.9-1.el7.x86_64.rpm 
安装qpress
tar xvf qpress-11-linux-x64.tar
cp qpress /usr/bin

挂载到nfs  
mount 10.106.241.126:/mnt/JiNan_NAS/DATABASES/027-013 /home/dbmanager

挂载命令写入启动脚本

vi /etc/rc.local



mkdir /home/dbmanager/backupfiles
mkdir /home/dbmanager/ibdfrms
chown -R dbmanager /home/dbmanager

sermod -G mysql dbmanager 

usermod -G root dbmanager

 CREATE USER 'yearning'@'%' IDENTIFIED BY 'yearning202110';
grant all privileges on *.* to 'yearning'@'%' identified by 'yearning202110';

chmod  a+rx -R /home/mysql/data
chmod  a+rx -R /data/mysql/data



