package StevenDimDoors.mod_pocketDimClient;

import StevenDimDoors.mod_pocketDim.core.PocketManager;
import StevenDimDoors.mod_pocketDim.watcher.ClientDimData;
import StevenDimDoors.mod_pocketDim.watcher.ClientLinkData;
import StevenDimDoors.mod_pocketDim.watcher.IUpdateWatcher;

public class ClientPacketHandler
{
	//TODO 1.7
	private IUpdateWatcher<ClientLinkData> linkWatcher;
	private IUpdateWatcher<ClientDimData> dimWatcher;
	
	/*
	public ClientPacketHandler()
	{
		PocketManager.getWatchers(this);
	}


	@Override
	public void registerWatchers(IUpdateWatcher<ClientDimData> dimWatcher, IUpdateWatcher<ClientLinkData> linkWatcher)
	{
		this.dimWatcher = dimWatcher;
		this.linkWatcher = linkWatcher;
	}
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		if (!packet.channel.equals(PacketConstants.CHANNEL_NAME))
			return;		
	
		//Checking memory connection wasnt working for some reason, but this seems to work fine. 
		if (FMLCommonHandler.instance().getMinecraftServerInstance() instanceof IntegratedServer)
			return;
		
		try
		{
			DataInputStream input = new DataInputStream(new ByteArrayInputStream(packet.data));
			byte packetID = input.readByte();
			switch (packetID)
			{
				case PacketConstants.CLIENT_JOIN_PACKET_ID:
					PocketManager.readPacket(input);
					break;
				case PacketConstants.CREATE_DIM_PACKET_ID:
					dimWatcher.onCreated( ClientDimData.read(input) );
					break;
				case PacketConstants.CREATE_LINK_PACKET_ID:
					linkWatcher.onCreated( ClientLinkData.read(input) );
					break;
				case PacketConstants.DELETE_DIM_PACKET_ID:
					dimWatcher.onDeleted( ClientDimData.read(input) );
					break;
				case PacketConstants.DELETE_LINK_PACKET_ID:
					linkWatcher.onDeleted( ClientLinkData.read(input) );
					break;
				case PacketConstants.UPDATE_LINK_PACKET_ID:
					linkWatcher.update( ClientLinkData.read(input) );
					break;
			}
		}
		catch (Exception e)
		{
			System.err.println("An exception occurred while processing a data packet:");
			e.printStackTrace();
		}
	}
	*/
}
