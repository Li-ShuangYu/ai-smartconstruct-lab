export interface Department {
  id: string
  deptName: string
}

export interface Major {
  id: string
  deptId: string
  majorName: string
}

export interface AdminClass {
  id: string
  majorId: string
  className: string
}