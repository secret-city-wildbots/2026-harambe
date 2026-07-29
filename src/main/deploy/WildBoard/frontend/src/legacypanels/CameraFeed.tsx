import { h, Fragment } from "preact";

import { Socket } from "socket.io-client";

interface CameraFeedProps {
    socket: Socket;
    dashboardItem?: boolean;
}

const CameraFeed = ({ socket, dashboardItem = false }: CameraFeedProps) => {

    return (
        <div>
        </div>
    );
};

export default CameraFeed;