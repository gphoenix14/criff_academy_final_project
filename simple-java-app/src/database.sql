-- Creazione della tabella 'users'
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    pwd VARCHAR(255) NOT NULL,
    isStealth BOOLEAN NOT NULL
);

CREATE TABLE groups (
    group_id SERIAL PRIMARY KEY,
    group_name VARCHAR(255) NOT NULL UNIQUE,
    enigmaPSK VARCHAR(255),
    aesPSK VARCHAR(255),
    cesarshift INT,
    defaultCrypto INT NOT NULL
);

CREATE TABLE users_groups (
    users_groups_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    group_id INT NOT NULL,
    isOwner BOOLEAN NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(group_id) ON DELETE CASCADE
);

-- Creazione della tabella 'connection'
CREATE TABLE connection (
    id_connection SERIAL PRIMARY KEY,
    public_ip INET NOT NULL,
    source_port INTEGER NOT NULL,
    isConnected BOOLEAN NOT NULL
);

-- Creazione della tabella 'files'
CREATE TABLE files (
    id_file SERIAL PRIMARY KEY,
    sender_id INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Creazione della tabella 'sessions'
CREATE TABLE sessions (
    id_session SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    start_session_date TIMESTAMP NOT NULL,
    end_session_date TIMESTAMP NOT NULL,
    id_connection INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (id_connection) REFERENCES connection(id_connection) ON DELETE CASCADE
);

CREATE TABLE messages (
    id_message SERIAL PRIMARY KEY,
    sender_id INTEGER NOT NULL,
    isUnicast BOOLEAN NOT NULL,
    isMulticast BOOLEAN NOT NULL,
    isBroadcast BOOLEAN NOT NULL,
    group_dst_id INT NOT NULL,
    hasAttachment BOOLEAN NOT NULL,
    attachment_id INT NOT NULL,
    msg_text TEXT NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_dst_id) REFERENCES groups(group_id) ON DELETE CASCADE,
    FOREIGN KEY (attachment_id) REFERENCES files(id_file) ON DELETE CASCADE
);
