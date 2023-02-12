
function echo_custom {
  case "$1" in
    "red")
      color="31"
      ;;
    "blue")
      color="34"
      ;;
    "yellow")
      color="33"
      ;;
    "green")
      color="32"
      ;;
    *)
      color="0"
      ;;
  esac

  echo -e "\033[0;${color}m$2\033[0m"
}



# 部署的服务器ip
remote_ip=192.168.5.185
# 服务器的用户
remote_user=devs
# 服务器上项目的部署路径
remote_path=/home/devs/ai-aiming/app
# 部署的环境
env=prod # dev,pre,prod,test


echo_custom "red" ">>> start deploy $env, 目标ip是 $remote_ip， 部署环境是 $env，请确认已正确构建jar包. <<<"

read -p "按任意键继续. 按 Ctrl+C 取消"


echo_custom "yellow" ">>> delete all files in $remote_path"

ssh ${remote_user}@${remote_ip} "cd ${remote_path} && rm -rf *"

echo_custom "yellow" ">>> copy jar files to $remote_path"

scp -r ./sa-common/target/$env-sa-common.jar $remote_user@$remote_ip:$remote_path/sa-common.jar
scp -r ./sa-admin/target/$env-sa-admin.jar $remote_user@$remote_ip:$remote_path/sa-admin.jar

echo_custom "yellow" ">>> copy config files to $remote_path"

scp -r ./docker/** ${remote_user}@${remote_ip}:${remote_path}

echo_custom "yellow" ">>> build docker image and start container"

ssh  $remote_user@$remote_ip "cd $remote_path \
  && docker rm -f app_smart-admin_1 \
  && docker rmi -f app_smart-admin \
  && docker-compose up -d \
  && docker logs -f app_smart-admin_1"