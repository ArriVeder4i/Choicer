package com.example.choicer.data

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID

@SuppressLint("MissingPermission")
object BleManager {

    var onReceive: (String) -> Unit = {}
    var getData: () -> String = { "" }
    var onDeviceFound: (BluetoothDevice, String?) -> Unit = { _, _ -> } // 🔥 НОВЫЙ КОЛЛБЕК ДЛЯ СПИСКА

    private val SERVICE_UUID: UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val CHAR_UUID: UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    private var bluetoothManager: BluetoothManager? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var gattServer: BluetoothGattServer? = null
    private var bleScanner: BluetoothLeScanner? = null
    private var appContext: Context? = null
    private var isRunning = false

    private val advertiseCallback = object : AdvertiseCallback() {}

    fun start(context: Context) {
        if (isRunning) return
        appContext = context.applicationContext
        bluetoothManager = appContext?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) return

        isRunning = true
        startGattServer()
        startAdvertising()
        startScanning()
    }

    fun stop() {
        if (!isRunning) return
        try {
            bleScanner?.stopScan(scanCallback)
            bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
            gattServer?.close()
            gattServer = null
            isRunning = false
        } catch (e: Exception) {
            Log.e("BleManager", "Ошибка при остановке", e)
        }
    }

    private fun startGattServer() {
        gattServer = bluetoothManager?.openGattServer(appContext, object : BluetoothGattServerCallback() {
            override fun onCharacteristicReadRequest(
                device: BluetoothDevice, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic
            ) {
                if (characteristic.uuid == CHAR_UUID) {
                    val dataBytes = getData().toByteArray(Charsets.UTF_8)
                    val response = if (offset < dataBytes.size) dataBytes.sliceArray(offset until dataBytes.size) else byteArrayOf()
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response)
                }
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice, requestId: Int, characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?
            ) {
                if (characteristic.uuid == CHAR_UUID && value != null) {
                    val receivedData = value.toString(Charsets.UTF_8)
                    if (receivedData.isNotEmpty()) onReceive(receivedData)
                    if (responseNeeded) gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        })

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val char = BluetoothGattCharacteristic(
            CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(char)
        gattServer?.addService(service)
    }

    private fun startAdvertising() {
        val advertiser = bluetoothAdapter?.bluetoothLeAdvertiser ?: return
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    private fun startScanning() {
        bleScanner = bluetoothAdapter?.bluetoothLeScanner
        val filter = ScanFilter.Builder().setServiceUuid(ParcelUuid(SERVICE_UUID)).build()
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bleScanner?.startScan(listOf(filter), settings, scanCallback)
    }

    // 🔥 ИЗМЕНЕНИЕ ЗДЕСЬ: Просто передаем устройство наверх, не подключаемся сами
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            onDeviceFound(result.device, result.scanRecord?.deviceName)
        }
    }

    // 🔥 НОВЫЙ МЕТОД: Запуск подключения по клику из списка
    fun connectToDevice(device: BluetoothDevice) {
        device.connectGatt(appContext, false, gattClientCallback)
    }

    private val gattClientCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) gatt.requestMtu(512)
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) gatt.close()
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val char = gatt.getService(SERVICE_UUID)?.getCharacteristic(CHAR_UUID)
            if (char != null) gatt.readCharacteristic(char)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val friendData = characteristic.value.toString(Charsets.UTF_8)
                if (friendData.isNotEmpty()) {
                    onReceive(friendData)
                    val myData = getData().toByteArray(Charsets.UTF_8)
                    characteristic.value = myData
                    gatt.writeCharacteristic(characteristic)
                }
            }
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            gatt.disconnect()
        }
    }
}
