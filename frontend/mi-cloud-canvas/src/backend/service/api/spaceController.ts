// @ts-ignore
/* eslint-disable */
import request from '@/backend/request'

/** addSpace POST /api/space/add */
export async function addSpaceUsingPost(body: API.SpaceAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong_>('/api/space/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** changeSpaceType POST /api/space/change/type */
export async function changeSpaceTypeUsingPost(body: API.SpaceTypeModifyRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/space/change/type', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editSpace POST /api/space/edit */
export async function editSpaceUsingPost(body: API.SpaceEditRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/space/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getSpaceVOByUserId GET /api/space/get/id/vo */
export async function getSpaceVoByUserIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getSpaceVOByUserIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseSpaceVO_>('/api/space/get/id/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listSpaceLevelInfo GET /api/space/list/space/level */
export async function listSpaceLevelInfoUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListSpaceLevelInfo_>('/api/space/list/space/level', {
    method: 'GET',
    ...(options || {}),
  })
}

/** updateSpace POST /api/space/update */
export async function updateSpaceUsingPost(body: API.SpaceUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/space/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
