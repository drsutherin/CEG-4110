# chat Package

The chat package will contain classes that manage user chats over the server.

## LobbyChat
The LobbyChat class controls the user's access a public chat that is available to all users on the server who are in the lobby.  There will be only one instance of this class.

## PrivateChat
The PrivateChat class controls a user's access to one-on-one chats with other users on the server.  There can be many PrivateChat instances, meaning a user can participate in multiple private chats at once.

*TODO: Give broad overview of integration with other packages*
