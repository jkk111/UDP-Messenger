# UDP-Messenger

Routing Protocols for Disaster Zones

Provides a routing protocol on top of udp for sending messages between client and server devices

Implements a Json-like format for serialization.
Implements a custom logger used in debugging.

Messages must be sent from Phone(client) to Server(laptop) and handle packet loss reliably (esp important as udp is lossy).
Uses a stop and wait method to handle packet loss.
