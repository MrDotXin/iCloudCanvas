// @ts-ignore
/* eslint-disable */
import request from '@/backend/request'

/** listPictureCategoryAll GET /api/picture/category/all */
export async function listPictureCategoryAllUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListString_>('/api/picture/category/all', {
    method: 'GET',
    ...(options || {}),
  })
}

/** deletePicture POST /api/picture/delete */
export async function deletePictureUsingPost(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/picture/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** editPicture POST /api/picture/edit */
export async function editPictureUsingPost(body: API.PictureEditRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/picture/edit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** getPictureById GET /api/picture/get/id */
export async function getPictureByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePicture_>('/api/picture/get/id', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** getPictureVOById GET /api/picture/getVO/id */
export async function getPictureVoByIdUsingGet(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPictureVOByIdUsingGETParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureVO_>('/api/picture/getVO/id', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** listPictureByPage POST /api/picture/list/page */
export async function listPictureByPageUsingPost(body: API.PictureQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePagePicture_>('/api/picture/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureVOByPage POST /api/picture/list/page/vo */
export async function listPictureVoByPageUsingPost(body: API.PictureQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePagePictureVO_>('/api/picture/list/page/vo', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** doPictureReview POST /api/picture/review */
export async function doPictureReviewUsingPost(body: API.PictureReviewRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/picture/review', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** listPictureTagCategory GET /api/picture/tag_category */
export async function listPictureTagCategoryUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponsePictureTagCategory_>('/api/picture/tag_category', {
    method: 'GET',
    ...(options || {}),
  })
}

/** listPictureTagAll GET /api/picture/tags/all */
export async function listPictureTagAllUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseListString_>('/api/picture/tags/all', {
    method: 'GET',
    ...(options || {}),
  })
}

/** updatePicture POST /api/picture/update */
export async function updatePictureUsingPost(body: API.PictureUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean_>('/api/picture/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** uploadPicture POST /api/picture/upload */
export async function uploadPictureUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureUsingPOSTParams,
  body: {},
  file?: File,
  options?: { [key: string]: any }
) {
  const formData = new FormData()

  if (file) {
    formData.append('file', file)
  }

  Object.keys(body).forEach((ele) => {
    const item = (body as any)[ele]

    if (item !== undefined && item !== null) {
      if (typeof item === 'object' && !(item instanceof File)) {
        if (item instanceof Array) {
          item.forEach((f) => formData.append(ele, f || ''))
        } else {
          formData.append(ele, JSON.stringify(item))
        }
      } else {
        formData.append(ele, item)
      }
    }
  })

  return request<API.BaseResponsePictureVO_>('/api/picture/upload', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    requestType: 'form',
    ...(options || {}),
  })
}

/** uploadPictureByUrl POST /api/picture/upload/url */
export async function uploadPictureByUrlUsingPost(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.uploadPictureByUrlUsingPOSTParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePictureVO_>('/api/picture/upload/url', {
    method: 'POST',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** grabPicturesByPrompt POST /api/picture/upload/url/batch */
export async function grabPicturesByPromptUsingPost(body: API.PictureUploadBatchRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseListPictureVO_>('/api/picture/upload/url/batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}
