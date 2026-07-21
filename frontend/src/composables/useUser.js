import { ref, computed } from 'vue'

// 预置的测试账号列表
const defaultUsers = [
  { id: 'xfg01', name: '测试用户01', role: '普通用户' },
  { id: 'xfg02', name: '测试用户02', role: '普通用户' },
  { id: 'xfg03', name: '测试用户03', role: '普通用户' },
  { id: 'xfg04', name: '测试用户04', role: '普通用户' },
  { id: 'xfg05', name: '测试用户05', role: '普通用户' },
  { id: 'xfg_new', name: '新用户', role: '普通用户' },
  { id: 'xfg_test1', name: '测试账号1', role: '测试' },
  { id: 'xfg_test2', name: '测试账号2', role: '测试' },
]

// 从 localStorage 读取用户列表
function loadUsers() {
  const saved = localStorage.getItem('gb_users')
  if (saved) {
    try {
      return JSON.parse(saved)
    } catch {
      return defaultUsers
    }
  }
  return defaultUsers
}

// 从 localStorage 读取当前用户
function loadCurrentUserId() {
  return localStorage.getItem('gb_current_user') || 'xfg01'
}

const users = ref(loadUsers())
const currentUserId = ref(loadCurrentUserId())

export function useUser() {
  // 当前用户对象
  const currentUser = computed(() => {
    return users.value.find(u => u.id === currentUserId.value) || users.value[0]
  })

  // 切换用户
  function switchUser(userId) {
    currentUserId.value = userId
    localStorage.setItem('gb_current_user', userId)
  }

  // 添加自定义用户
  function addUser(id, name, role = '自定义') {
    if (!users.value.find(u => u.id === id)) {
      users.value.push({ id, name, role })
      localStorage.setItem('gb_users', JSON.stringify(users.value))
    }
  }

  // 移除用户
  function removeUser(id) {
    const idx = users.value.findIndex(u => u.id === id)
    if (idx > -1) {
      users.value.splice(idx, 1)
      localStorage.setItem('gb_users', JSON.stringify(users.value))
      // 如果删除的是当前用户，切换到第一个
      if (currentUserId.value === id) {
        switchUser(users.value[0]?.id || 'xfg01')
      }
    }
  }

  // 重置为默认用户
  function resetUsers() {
    users.value = [...defaultUsers]
    localStorage.setItem('gb_users', JSON.stringify(users.value))
    switchUser('xfg01')
  }

  return {
    users,
    currentUserId,
    currentUser,
    switchUser,
    addUser,
    removeUser,
    resetUsers,
  }
}
