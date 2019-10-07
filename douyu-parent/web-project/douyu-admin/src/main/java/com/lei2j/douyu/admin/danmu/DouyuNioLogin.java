package com.lei2j.douyu.admin.danmu;

import com.lei2j.douyu.admin.danmu.config.DouyuMessageConfig;
import com.lei2j.douyu.core.config.DouyuAddress;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author lei2j
 */
public class DouyuNioLogin extends AbstractDouyuLogin {
	
	private SocketChannel socketChannel;

	private DouyuNioConnection douyuNioConnection;

	public DouyuNioLogin(Integer room,DouyuNioConnection douyuNioConnection) {
		super(room);
		this.douyuNioConnection = douyuNioConnection;
	}

	/**
	 * @throws IOException IOException
	 */
	@Override
	public boolean login() throws IOException {
		DouyuDanmuLoginAuth danmuLoginAuth = getChatServerAddress();
		if (danmuLoginAuth == null) {
			return false;
		}
		this.douyuAddress = danmuLoginAuth.getAddress();
		String username = danmuLoginAuth.getUsername();
		logger.info("开始连接弹幕服务器:{}:{}", douyuAddress.getIp(), douyuAddress.getPort());
		SocketChannel channel = SelectorProvider.provider().openSocketChannel();
		douyuNioConnection.register(this, channel);
		this.socketChannel = channel;
		douyuNioConnection.write(DouyuMessageConfig.getLoginMessage(room, username, "1234567890123456"), channel);
		join();
		if (keepaliveSchedule != null) {
			keepaliveSchedule.cancel();
		}
		keepaliveSchedule = new KeepaliveSchedule();
		keepaliveSchedule.schedule(() -> {
			try {
				if (!this.socketChannel.isConnected() || !this.socketChannel.isOpen()) {
					logger.info("房间{}|心跳检测停止", room);
					keepaliveSchedule.cancel();
					return;
				}
				douyuNioConnection.write(DouyuMessageConfig.getKeepaliveMessage(), channel);
				logger.info("房间{}|发送心跳检测{}", room, LocalDateTime.now());
			} catch (Exception e) {
				keepaliveSchedule.cancel();
				logger.info("房间{}|心跳检测停止", room);
				if (this.socketChannel.isOpen() || this.socketChannel.isConnected()) {
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	@Override
	protected DouyuDanmuLoginAuth getChatServerAddress(String username, String password) throws IOException {
		DouyuAddress douyuAddress = DouyuMessageConfig.getLoginServerAddress(room);
		DouyuNioConnection douyuConnection = this.douyuNioConnection;
		SocketAddress serverAddress = new InetSocketAddress(douyuAddress.getIp(), douyuAddress.getPort());
		SocketChannel socketChannel = SelectorProvider.provider().openSocketChannel();
		socketChannel.connect(serverAddress);
		douyuConnection.write(DouyuMessageConfig.getLoginMessage(room, username, password), socketChannel);
		Map<String, Object> loginMessageMap = douyuConnection.read(socketChannel);
		Map<String, Object> address = douyuConnection.read(socketChannel);
		socketChannel.close();
		return getLoginAuth(loginMessageMap, address);
	}

	private void join()throws IOException{
        //加入房间分组
		douyuNioConnection.write(DouyuMessageConfig.getJoinMessage(room),socketChannel);
		logger.info("房间{}|连接成功",room);
    }
	
	@Override
	public void logout() {
		try {
			if (!socketChannel.isOpen()) {
				return;
			}
			synchronized (this){
				douyuNioConnection.write(DouyuMessageConfig.getLogoutMessage(),socketChannel);
				socketChannel.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			keepaliveSchedule.cancel();
			cacheRoomService.remove(room);
			logger.info("房间{}|成功退出",room);
		}
	}

	@Override
	public void retry(){
		logger.info("重新连接房间:{}",room);
		try {
			login();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
