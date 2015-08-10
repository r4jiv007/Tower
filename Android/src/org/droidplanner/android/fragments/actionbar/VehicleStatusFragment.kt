package org.droidplanner.android.fragments.actionbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent
import com.o3dr.services.android.lib.drone.attribute.AttributeType
import com.o3dr.services.android.lib.drone.property.Battery
import com.o3dr.services.android.lib.drone.property.State
import org.droidplanner.android.R
import org.droidplanner.android.fragments.helpers.ApiListenerFragment
import kotlin.properties.Delegates

/**
 * Created by Fredia Huya-Kouadio on 7/24/15.
 */
public class VehicleStatusFragment : ApiListenerFragment() {

    companion object {
        private val filter = initFilter()

        private fun initFilter(): IntentFilter {
            val filter = IntentFilter()

            filter.addAction(AttributeEvent.STATE_CONNECTED)
            filter.addAction(AttributeEvent.STATE_DISCONNECTED)
            filter.addAction(AttributeEvent.HEARTBEAT_TIMEOUT)
            filter.addAction(AttributeEvent.HEARTBEAT_RESTORED)
            filter.addAction(AttributeEvent.BATTERY_UPDATED)

            return filter
        }
    }

    private val receiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.getAction()){
                AttributeEvent.STATE_CONNECTED -> updateAllStatus()

                AttributeEvent.STATE_DISCONNECTED -> updateAllStatus()

                AttributeEvent.HEARTBEAT_RESTORED -> updateConnectionStatus()

                AttributeEvent.HEARTBEAT_TIMEOUT -> updateConnectionStatus()

                AttributeEvent.BATTERY_UPDATED -> updateBatteryStatus()
            }
        }
    }

    private val connectedIcon by Delegates.lazy {
        getView()?.findViewById(R.id.status_vehicle_connection) as ImageView?
    }

    private val batteryIcon by Delegates.lazy {
        getView()?.findViewById(R.id.status_vehicle_battery) as ImageView?
    }

    private val titleView by Delegates.lazy {
        getView()?.findViewById(R.id.status_actionbar_title) as TextView?
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater?.inflate(R.layout.fragment_vehicle_status, container, false)
    }

    override fun onApiConnected() {
        updateAllStatus()
        getBroadcastManager().registerReceiver(receiver, filter)
    }

    override fun onApiDisconnected() {
        getBroadcastManager().unregisterReceiver(receiver)
        updateAllStatus()
    }

    private fun updateAllStatus(){
        updateBatteryStatus()
        updateConnectionStatus()
    }

    private fun updateConnectionStatus() {
        val drone = getDrone()
        connectedIcon?.setImageLevel(
                if(drone == null || !drone.isConnected())
                    0
                else {
                    val state: State = drone.getAttribute(AttributeType.STATE)
                    if (state.isTelemetryLive())
                        2
                    else
                        1
                }
        )
    }

    fun setTitle(titleResId: Int){
        titleView?.setText(titleResId)
    }

    fun setTitle(title: CharSequence){
        titleView?.setText(title)
    }

    private fun updateBatteryStatus() {
        val drone = getDrone()
        batteryIcon?.setImageLevel(
                if(drone == null || !drone.isConnected()){
                    0
                }
                else{
                    val battery: Battery = drone.getAttribute(AttributeType.BATTERY)
                    val battRemain = battery.getBatteryRemain()

                    if (battRemain >= 100) {
                        8
                    } else if (battRemain >= 87.5) {
                        7
                    } else if (battRemain >= 75) {
                        6
                    } else if (battRemain >= 62.5) {
                        5
                    } else if (battRemain >= 50) {
                        4
                    } else if (battRemain >= 37.5) {
                        3
                    } else if (battRemain >= 25) {
                        2
                    } else if (battRemain >= 12.5) {
                        1
                    } else {
                        0
                    }
                }
        )
    }
}