export const demoUsers = [
  {
    username: 'admin',
    password: 'admin123',
    name: 'Avery Stone',
    role: 'ADMIN'
  },
  {
    username: 'user1',
    password: 'user123',
    name: 'Jordan Lee',
    role: 'USER'
  }
];

export function authenticate(username, password) {
  const user = demoUsers.find((item) => item.username === username && item.password === password);
  if (!user) {
    return null;
  }
  return {
    username: user.username,
    name: user.name,
    role: user.role
  };
}
