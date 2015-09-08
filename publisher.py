import paho.mqtt.client as mqtt

def on_connect(client, userdata, flags, rc):
    client.subscribe("vps/metrics")

client = mqtt.Client()

client.on_connect = on_connect

client.will_set("vps/meta", "device_id=foobar;disconnect", 0, False)

client.connect("localhost", 1883, 60)

client.publish("vps/meta", "device_id=foobar;connect", 0, False)
client.publish("vps/metrics", "device_id=foobar;temperature=98.6;current=12.34;voltage=45.788", 0, False)

client.loop_forever()