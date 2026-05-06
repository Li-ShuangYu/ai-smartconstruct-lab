export interface Department {
  id: number
  deptName: string
}

export interface Major {
  id: number
  deptId: number
  majorName: string
}

export interface AdminClass {
  id: number
  majorId: number
  className: string
}
