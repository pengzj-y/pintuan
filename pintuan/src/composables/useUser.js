import { ref, computed } from 'vue'

// 预置的测试账号列表（模拟不同客户）
const defaultUsers = [
  { id: 'xfg01', name: '小明', avatar: '😀', role: '客户' },
  { id: 'xfg02', name: '小红', avatar: '🥰', role: '客户' },
  { id: 'xfg03', name: '小李', avatar: '😎', role: '客户' },
  { id: 'xfg04', name: '小王', avatar: '🤩', role: '客户' },
  { id: 'xfg05', name: '小赵', avatar: '😄', role: '客户' },
  { id: 'xfg_new', name: '新用户', avatar: '🆕', role: '客户' },
  { id: 'xfg_test1', name: '测试账号1', avatar: '🧪', role: '测试' },
  { id: 'xfg_test2', name: '测试账号2', avatar: '🧪', role: '测试' },
]

function loadUsers() {
  const saved = localStorage.getItem('pt_users')
  if (saved) {
    try {
      return JSON.parse(saved)
    } catch {
      return defaultUsers
    }
  }
  return defaultUsers
}

function loadCurrentUserId() {
  return localStorage.getItem('pt_current_user') || 'xfg01'
}

const users = ref(loadUsers())
const currentUserId = ref(loadCurrentUserId())

export function useUser() {
  const currentUser = computed(() => {
    return users.value.find(u => u.id === currentUserId.value) || users.value[0]
  })

  function switchUser(userId) {
    currentUserId.value = userId
    localStorage.setItem('pt_current_user', userId)
  }

  function addUser(id, name, role = '自定义') {
    if (!users.value.find(u => u.id === id)) {
      users.value.push({ id, name, avatar: '🙂', role })
      localStorage.setItem('pt_users', JSON.stringify(users.value))
    }
  }

  function removeUser(id) {
    const idx = users.value.findIndex(u => u.id === id)
    if (idx > -1) {
      users.value.splice(idx, 1)
      localStorage.setItem('pt_users', JSON.stringify(users.value))
      if (currentUserId.value === id) {
        switchUser(users.value[0]?.id || 'xfg01')
      }
    }
  }

  function resetUsers() {
    users.value = [...defaultUsers]
    localStorage.setItem('pt_users', JSON.stringify(users.value))
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
