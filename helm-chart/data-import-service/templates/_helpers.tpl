{{/*
Expand the name of the chart.
*/}}
{{- define "data-import-service.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "data-import-service.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "data-import-service.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "data-import-service.labels" -}}
helm.sh/chart: {{ include "data-import-service.chart" . }}
{{ include "data-import-service.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- with .Values.commonLabels }}
{{ toYaml . }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "data-import-service.selectorLabels" -}}
app.kubernetes.io/name: {{ include "data-import-service.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "data-import-service.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "data-import-service.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
MySQL fullname
*/}}
{{- define "data-import-service.mysql.fullname" -}}
{{- printf "%s-mysql" (include "data-import-service.fullname" .) }}
{{- end }}

{{/*
Backend fullname
*/}}
{{- define "data-import-service.backend.fullname" -}}
{{- printf "%s-backend" (include "data-import-service.fullname" .) }}
{{- end }}

{{/*
Frontend fullname
*/}}
{{- define "data-import-service.frontend.fullname" -}}
{{- printf "%s-frontend" (include "data-import-service.fullname" .) }}
{{- end }}

{{/*
MySQL labels
*/}}
{{- define "data-import-service.mysql.labels" -}}
{{ include "data-import-service.labels" . }}
app.kubernetes.io/component: mysql
{{- end }}

{{/*
Backend labels
*/}}
{{- define "data-import-service.backend.labels" -}}
{{ include "data-import-service.labels" . }}
app.kubernetes.io/component: backend
{{- end }}

{{/*
Frontend labels
*/}}
{{- define "data-import-service.frontend.labels" -}}
{{ include "data-import-service.labels" . }}
app.kubernetes.io/component: frontend
{{- end }}

{{/*
MySQL selector labels
*/}}
{{- define "data-import-service.mysql.selectorLabels" -}}
{{ include "data-import-service.selectorLabels" . }}
app.kubernetes.io/component: mysql
{{- end }}

{{/*
Backend selector labels
*/}}
{{- define "data-import-service.backend.selectorLabels" -}}
{{ include "data-import-service.selectorLabels" . }}
app.kubernetes.io/component: backend
{{- end }}

{{/*
Frontend selector labels
*/}}
{{- define "data-import-service.frontend.selectorLabels" -}}
{{ include "data-import-service.selectorLabels" . }}
app.kubernetes.io/component: frontend
{{- end }}

{{/*
Create the database connection URL
*/}}
{{- define "data-import-service.mysql.connectionUrl" -}}
{{- printf "jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8" (include "data-import-service.mysql.fullname" .) (.Values.mysql.service.port | int) .Values.mysql.auth.database }}
{{- end }}

{{/*
Create backend service URL for frontend
*/}}
{{- define "data-import-service.backend.serviceUrl" -}}
{{- printf "http://%s:%d/data-import" (include "data-import-service.backend.fullname" .) (.Values.backend.service.port | int) }}
{{- end }}

{{/*
Image pull secrets
*/}}
{{- define "data-import-service.imagePullSecrets" -}}
{{- if .Values.global.imagePullSecrets }}
imagePullSecrets:
{{- range .Values.global.imagePullSecrets }}
  - name: {{ . }}
{{- end }}
{{- end }}
{{- end }}