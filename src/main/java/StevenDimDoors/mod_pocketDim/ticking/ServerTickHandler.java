
package StevenDimDoors.mod_pocketDim.ticking;


import StevenDimDoors.mod_pocketDim.core.DDTeleporter;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import java.util.ArrayList;

public class ServerTickHandler implements IRegularTickSender
{
	private static final String PROFILING_LABEL = "Dimensional Doors: Server Tick";
	
	private int tickCount = 0;
	private ArrayList<RegularTickReceiverInfo> receivers;

	public ServerTickHandler()
	{
		this.receivers = new ArrayList<RegularTickReceiverInfo>();
	}

	@Override
	public void registerReceiver(IRegularTickReceiver receiver, int interval, boolean onTickStart)
	{
		RegularTickReceiverInfo info = new RegularTickReceiverInfo(receiver, interval, onTickStart);
		receivers.add(info);
	}
	
	@Override
	public void unregisterReceivers()
	{
		receivers.clear();
	}

	@SubscribeEvent
	public void tickStart(TickEvent.WorldTickEvent event) {
		if(!event.phase.equals(TickEvent.Phase.START))
			return;

		for(RegularTickReceiverInfo info : receivers)
		{
			if( info.OnTickStart && tickCount % info.Interval == 0)
				info.RegularTickReceiver.notifyTick();
		}

		//TODO: Stuck this in here because it's already rather hackish.
		//We should standardize this as an IRegularTickReceiver in the future. ~SenseiKiwi
		if (DDTeleporter.cooldown > 0)
		{
			DDTeleporter.cooldown--;
		}

	}

	@SubscribeEvent
	public void tickEnd(TickEvent.WorldTickEvent event) {
		if(!event.phase.equals(TickEvent.Phase.END))
			return;

		for (RegularTickReceiverInfo info : receivers)
		{
			if (!info.OnTickStart && tickCount % info.Interval == 0)
			{
				info.RegularTickReceiver.notifyTick();
			}
		}
		tickCount++; //There is no need to reset the counter. Let it overflow.
	}

}
