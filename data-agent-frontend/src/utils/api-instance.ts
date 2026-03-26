import { Api } from '@/apis/__generated'
import { request } from '@/utils/request'

export const api = new Api(async ({ uri, method, body }) => {
  return await request({ url: uri, method, data: body })
})
