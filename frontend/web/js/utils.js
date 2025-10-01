// Utilitários para manipulação de datas e validações
export const DateUtils = {
    
    // Verifica se um ano é bissexto
    isLeapYear(year) {
        return (year % 4 === 0 && year % 100 !== 0) || (year % 400 === 0);
    },

    // Converte Date para dia juliano (DDD)
    dateToJulianDay(date) {
        const start = new Date(date.getFullYear(), 0, 0);
        const diff = date - start;
        const oneDay = 1000 * 60 * 60 * 24;
        return Math.floor(diff / oneDay);
    },

    // Converte dia juliano para Date
    julianDayToDate(year, day) {
        const date = new Date(year, 0, day);
        return date;
    },

    // Valida ano (1995 até ano atual)
    validateYear(year) {
        const currentYear = new Date().getFullYear();
        const numYear = parseInt(year);
        return numYear >= 1995 && numYear <= currentYear;
    },

    // Valida dia (001-365/366)
    validateDay(year, day) {
        const numYear = parseInt(year);
        const numDay = parseInt(day);
        const maxDay = this.isLeapYear(numYear) ? 366 : 365;
        return numDay >= 1 && numDay <= maxDay;
    },

    // Formata dia com zeros à esquerda (001, 002, etc.)
    formatDay(day) {
        return String(day).padStart(3, '0');
    },

    // Obtém data de hoje como {ano, dia}
    getTodayJulian() {
        const today = new Date();
        return {
            year: today.getFullYear(),
            day: this.dateToJulianDay(today)
        };
    },

    // Obtém data de ontem como {ano, dia}
    getYesterdayJulian() {
        const yesterday = new Date();
        yesterday.setDate(yesterday.getDate() - 1);
        return {
            year: yesterday.getFullYear(),
            day: this.dateToJulianDay(yesterday)
        };
    },

    // Valida entrada completa de data
    validateDateInput(year, day) {
        const errors = [];
        
        if (!this.validateYear(year)) {
            errors.push(`Ano deve estar entre 1995 e ${new Date().getFullYear()}`);
        }
        
        if (!this.validateDay(year, day)) {
            const maxDay = this.isLeapYear(parseInt(year)) ? 366 : 365;
            errors.push(`Dia deve estar entre 1 e ${maxDay}${this.isLeapYear(parseInt(year)) ? ' (ano bissexto)' : ''}`);
        }
        
        return {
            valid: errors.length === 0,
            errors: errors
        };
    }
};

// Utilitários para formatação e validação geral
export const FormatUtils = {
    
    // Formata coordenadas para exibição
    formatCoordinate(coord, precision = 6) {
        return parseFloat(coord).toFixed(precision);
    },

    // Formata timestamp para exibição local
    formatDateTime(timestamp) {
        if (!timestamp) return 'N/A';
        return new Date(timestamp).toLocaleString('pt-BR');
    },

    // Formata duração em segundos para texto legível
    formatDuration(seconds) {
        if (seconds < 60) return `${seconds}s`;
        if (seconds < 3600) return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return `${hours}h ${minutes}m`;
    },

    // Valida código de estação (4 letras)
    validateStationCode(code) {
        return /^[A-Za-z]{4}$/.test(code);
    },

    // Formata tamanho de arquivo
    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
};

// Utilitários para URLs e downloads
export const UrlUtils = {
    
    // Constrói URL para relatório RBMC
    buildReportUrl(station) {
        return `/api/v1/rbmc/${station.toUpperCase()}/relatorio`;
    },

    // Constrói URL para RINEX2
    buildRinex2Url(station, year, day) {
        return `/api/v1/rbmc/rinex2/${station.toUpperCase()}/${year}/${DateUtils.formatDay(day)}`;
    },

    // Constrói URL para RINEX3 15s
    buildRinex3Url(station, year, day) {
        return `/api/v1/rbmc/rinex3/${station.toUpperCase()}/${year}/${DateUtils.formatDay(day)}`;
    },

    // Constrói URL para RINEX3 1s
    buildRinex3_1sUrl(station, year, day, hour, minute, type = 'MO') {
        return `/api/v1/rbmc/rinex3/1s/${station.toUpperCase()}/${year}/${DateUtils.formatDay(day)}/${String(hour).padStart(2, '0')}/${String(minute).padStart(2, '0')}/${type.toUpperCase()}`;
    },

    // Constrói URL para órbitas
    buildOrbitUrl(year, day) {
        return `/api/v1/rbmc/rinex3/orbitas/${year}/${DateUtils.formatDay(day)}`;
    },

    // Abre URL em nova aba
    openInNewTab(url) {
        window.open(url, '_blank', 'noopener,noreferrer');
    },

    // Download via fetch com fallback para nova aba
    async downloadFile(url, filename) {
        try {
            const response = await fetch(url);
            if (response.ok) {
                const blob = await response.blob();
                const downloadUrl = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = downloadUrl;
                a.download = filename || 'download';
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(downloadUrl);
                return true;
            } else {
                // Fallback: abrir em nova aba
                this.openInNewTab(url);
                return false;
            }
        } catch (error) {
            console.warn('Download direto falhou, abrindo em nova aba:', error);
            this.openInNewTab(url);
            return false;
        }
    }
};

// Debounce para evitar chamadas excessivas
export function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle para limitar frequência de chamadas
export function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}