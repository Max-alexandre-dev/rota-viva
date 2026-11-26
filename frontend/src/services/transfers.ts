export type TransferStatus = 'PROPOSTA' | 'APROVADA' | 'REJEITADA' | 'CANCELADA'
export type StorageMode = 'DEMO_IN_MEMORY' | (string & {})

export interface Transfer {
  id: string
  originCenterId: string
  destinationCenterId: string
  resourceId: string
  quantity: number
  reason: string
  status: TransferStatus
  storageMode: StorageMode
}

export interface TransferListResponse {
  storageMode: StorageMode
  transfers: Transfer[]
}

export interface TransferCreateRequest {
  originCenterId: string
  destinationCenterId: string
  resourceId: string
  quantity: number
  reason: string
}

export interface ApiErrorResponse {
  code: string
  message: string
  details: Record<string, string>
  storageMode: StorageMode
}

export class TransferApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
    readonly details: Record<string, string> = {},
    readonly storageMode?: StorageMode,
  ) {
    super(message)
    this.name = 'TransferApiError'
  }

  get isConflict(): boolean {
    return this.status === 409 || this.code === 'TRANSFER_CONFLICT'
  }
}

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${apiBaseUrl}${path}`, init)
  } catch {
    throw new TransferApiError('Não foi possível conectar à API de transferências.', 0, 'NETWORK_ERROR')
  }

  const body: unknown = await response.json().catch(() => undefined)
  if (!response.ok) {
    const error = body as Partial<ApiErrorResponse> | undefined
    throw new TransferApiError(
      error?.message || `A API respondeu com o status ${response.status}.`,
      response.status,
      error?.code,
      error?.details ?? {},
      error?.storageMode,
    )
  }
  return body as T
}

function idempotencyHeaders(): HeadersInit {
  return { 'Idempotency-Key': crypto.randomUUID() }
}

export const transfersService = {
  list(): Promise<TransferListResponse> {
    return request<TransferListResponse>('/transfers')
  },

  create(input: TransferCreateRequest): Promise<Transfer> {
    return request<Transfer>('/transfers', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...idempotencyHeaders() },
      body: JSON.stringify(input),
    })
  },

  approve(id: string): Promise<Transfer> {
    return request<Transfer>(`/transfers/${encodeURIComponent(id)}/approve`, {
      method: 'POST',
      headers: idempotencyHeaders(),
    })
  },

  reject(id: string): Promise<Transfer> {
    return request<Transfer>(`/transfers/${encodeURIComponent(id)}/reject`, {
      method: 'POST',
      headers: idempotencyHeaders(),
    })
  },
}
